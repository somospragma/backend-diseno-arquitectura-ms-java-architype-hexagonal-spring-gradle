package co.com.pragma.accounts.infrastructure.publisher;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import co.com.pragma.accounts.model.exception.TechnicalException;
import co.com.pragma.accounts.ports.IEventPublisher;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * Productor de eventos a SQS (cliente sincrono). Serializa el evento con un
 * esquema versionado (message attribute schemaVersion, MSG-O-004) para
 * consumidores idempotentes. Mapea el fallo tecnico sin propagar crudo.
 */
@Component
@ConditionalOnProperty(prefix = "sqs", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SqsEventPublisher implements IEventPublisher {

    private static final String SCHEMA_VERSION = "1";

    private final SqsClient sqsClient;
    private final EventPublisherProperties properties;

    @Override
    public void publish(AccountEvent event) {
        SendMessageRequest request = SendMessageRequest.builder()
            .queueUrl(properties.getQueueUrl())
            .messageBody(serialize(event))
            .messageAttributes(Map.of(
                "schemaVersion", MessageAttributeValue.builder()
                    .dataType("String").stringValue(SCHEMA_VERSION).build(),
                "eventType", MessageAttributeValue.builder()
                    .dataType("String").stringValue(event.type()).build()))
            .build();
        try {
            sqsClient.sendMessage(request);
        } catch (RuntimeException e) {
            throw new TechnicalException(
                ErrorCatalog.UNEXPECTED_ERROR.code(), "Event publish failed", e);
        }
    }

    private String serialize(AccountEvent event) {
        // Esquema estable y versionado (en un caso real, Jackson/Avro + schema registry).
        return "{\"schemaVersion\":\"" + SCHEMA_VERSION + "\","
            + "\"accountId\":\"" + event.accountId() + "\","
            + "\"eventId\":\"" + event.eventId() + "\","
            + "\"type\":\"" + event.type() + "\","
            + "\"occurredAt\":\"" + event.occurredAt() + "\"}";
    }
}
