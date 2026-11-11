package co.com.pragma.accounts.infrastructure.kafka;

import co.com.pragma.accounts.ports.IIdempotencyStore;
import java.time.Duration;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

/**
 * Consumidor Kafka imperativo (spring-kafka, @KafkaListener) con AISLAMIENTO DE
 * FALLOS POR REGISTRO (RES-O-003) e IDEMPOTENCIA (MSG-O-001). Reglas:
 *  - Cada registro se procesa aislado (try/catch); un fallo individual no rompe
 *    el listener.
 *  - Dedupe por (topic:partition:offset) antes de aplicar el efecto.
 *  - Ack MANUAL solo tras procesamiento exitoso o duplicado (MSG-O-003); ante
 *    fallo NO se hace ack: el offset no avanza y el registro se reprocesa (DLQ
 *    segun la politica del broker).
 */
@RequiredArgsConstructor
public class KafkaEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(KafkaEventConsumer.class);
    private static final Duration DEDUPE_TTL = Duration.ofHours(6);

    private final Consumer<ConsumerRecord<String, String>> processor;
    private final IIdempotencyStore idempotencyStore;

    @KafkaListener(
        topics = "${kafka.topic}",
        groupId = "${kafka.consumer-group}",
        containerFactory = "kafkaListenerContainerFactory")
    public void onMessage(ConsumerRecord<String, String> record, Acknowledgment ack) {
        handleRecordIsolated(record, ack);
    }

    /** Visible para test: procesa un registro aislado con idempotencia y ack manual. */
    public void handleRecordIsolated(ConsumerRecord<String, String> record, Acknowledgment ack) {
        String key = "kafka:" + record.topic() + ":" + record.partition() + ":" + record.offset();
        try {
            if (!idempotencyStore.markIfFirst(key, DEDUPE_TTL)) {
                log.info("Registro Kafka duplicado offset={}; se ignora", record.offset());
                ack.acknowledge();
                return;
            }
            processor.accept(record);
            ack.acknowledge(); // ack tras exito (MSG-O-003)
        } catch (RuntimeException error) {
            // No se hace ack: el offset no avanza, el registro se reprocesa/DLQ.
            log.error("Error procesando registro Kafka offset={}; NO se hace ack. El consumidor continua.",
                record.offset(), error);
        }
    }
}
