package co.com.pragma.accounts.infrastructure.sqs;

import co.com.pragma.accounts.ports.IIdempotencyStore;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

/**
 * Consumidor SQS imperativo (SqsClient sincrono) con AISLAMIENTO DE FALLOS POR
 * MENSAJE (RES-O-003) e IDEMPOTENCIA (MSG-O-001).
 *
 * Regla central: una excepcion procesando UN mensaje NUNCA rompe el consumidor.
 *  - Cada mensaje se procesa aislado (try/catch).
 *  - Un mensaje que falla NO se borra: vuelve a la cola y, tras maxReceiveCount,
 *    la redrive policy de SQS lo envia a la DLQ (MSG-O-002/003).
 *  - El poll loop corre en un hilo dedicado (long polling) y se detiene con stop().
 */
@RequiredArgsConstructor
public class SqsMessageConsumer {

    private static final Logger log = LoggerFactory.getLogger(SqsMessageConsumer.class);
    private static final Duration DEDUPE_TTL = Duration.ofHours(6);

    private final SqsClient sqsClient;
    private final String queueUrl;
    private final Consumer<Message> processor;
    /** Deduplicacion at-least-once por messageId (MSG-O-001). */
    private final IIdempotencyStore idempotencyStore;

    private volatile boolean running;
    private ExecutorService executor;

    public void start() {
        running = true;
        executor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "sqs-consumer");
            t.setDaemon(true);
            return t;
        });
        executor.submit(this::pollLoop);
    }

    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    private void pollLoop() {
        while (running) {
            try {
                for (Message message : receiveBatch()) {
                    handleMessageIsolated(message);
                }
            } catch (RuntimeException e) {
                log.warn("SQS poll loop error; se reintenta el siguiente ciclo", e);
            }
        }
    }

    /** Visible para test: recibe un batch (long polling). */
    List<Message> receiveBatch() {
        ReceiveMessageRequest request = ReceiveMessageRequest.builder()
            .queueUrl(queueUrl)
            .maxNumberOfMessages(10)
            .waitTimeSeconds(20)   // long polling
            .build();
        return sqsClient.receiveMessage(request).messages();
    }

    /**
     * Procesa un mensaje AISLADO: si falla, se registra y el mensaje NO se borra
     * (ira a DLQ por reintentos). El error se contiene aqui; jamas rompe el loop.
     */
    void handleMessageIsolated(Message message) {
        try {
            // Idempotencia (MSG-O-001): si el messageId ya se proceso, se salta el
            // efecto pero igual se borra (ya no hay nada que hacer con el duplicado).
            if (!idempotencyStore.markIfFirst("sqs:" + message.messageId(), DEDUPE_TTL)) {
                log.info("Mensaje {} duplicado (ya procesado); se ignora y se borra", message.messageId());
                deleteMessage(message);
                return;
            }
            processor.accept(message);
            deleteMessage(message); // solo se borra si el proceso fue exitoso (MSG-O-003)
        } catch (RuntimeException error) {
            log.error("Error procesando mensaje {}; NO se borra (ira a DLQ por reintentos). El consumidor continua.",
                message.messageId(), error);
        }
    }

    private void deleteMessage(Message message) {
        sqsClient.deleteMessage(DeleteMessageRequest.builder()
            .queueUrl(queueUrl)
            .receiptHandle(message.receiptHandle())
            .build());
    }
}
