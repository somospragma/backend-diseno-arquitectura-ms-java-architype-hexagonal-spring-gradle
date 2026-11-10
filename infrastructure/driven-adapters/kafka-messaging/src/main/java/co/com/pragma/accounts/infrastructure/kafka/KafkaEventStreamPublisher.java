package co.com.pragma.accounts.infrastructure.kafka;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import co.com.pragma.accounts.model.exception.TechnicalException;
import co.com.pragma.accounts.ports.IEventStreamPublisher;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;

/**
 * Productor de eventos a Kafka con spring-kafka (KafkaTemplate). Usa accountId
 * como clave de particion (preserva orden por agregado). Envio SINCRONO
 * (get()) para propagar el fallo como TechnicalException. El header
 * schemaVersion documenta el contrato (MSG-O-004).
 */
@RequiredArgsConstructor
public class KafkaEventStreamPublisher implements IEventStreamPublisher {

    private static final String SCHEMA_VERSION = "1";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    @Override
    public void publish(AccountEvent event) {
        ProducerRecord<String, String> record =
            new ProducerRecord<>(topic, event.accountId(), serialize(event));
        record.headers().add("schemaVersion", SCHEMA_VERSION.getBytes());
        record.headers().add("eventType", event.type().getBytes());
        try {
            kafkaTemplate.send(record).get(); // envio sincrono: confirma o falla
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new TechnicalException(
                ErrorCatalog.UNEXPECTED_ERROR.code(), "Kafka publish interrupted", e);
        } catch (ExecutionException e) {
            throw new TechnicalException(
                ErrorCatalog.UNEXPECTED_ERROR.code(), "Kafka publish failed", e);
        }
    }

    private String serialize(AccountEvent event) {
        return "{\"schemaVersion\":\"" + SCHEMA_VERSION + "\","
            + "\"accountId\":\"" + event.accountId() + "\","
            + "\"eventId\":\"" + event.eventId() + "\","
            + "\"type\":\"" + event.type() + "\","
            + "\"occurredAt\":\"" + event.occurredAt() + "\"}";
    }
}
