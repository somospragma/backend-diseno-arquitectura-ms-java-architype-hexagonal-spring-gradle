package co.com.pragma.accounts.infrastructure.sns;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import co.com.pragma.accounts.model.exception.TechnicalException;
import co.com.pragma.accounts.ports.INotificationPublisher;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.MessageAttributeValue;
import software.amazon.awssdk.services.sns.model.PublishRequest;

/**
 * Publisher de notificaciones fan-out a SNS (cliente sincrono). Adjunta el
 * esquema versionado como message attribute (MSG-O-004) y traduce el fallo
 * tecnico sin propagarlo crudo.
 */
@Component
@ConditionalOnProperty(prefix = "sns", name = "enabled", havingValue = "true")
@RequiredArgsConstructor
public class SnsNotificationPublisher implements INotificationPublisher {

    private static final String SCHEMA_VERSION = "1";

    private final SnsClient snsClient;
    private final SnsProperties properties;

    @Override
    public void notify(AccountEvent event) {
        PublishRequest request = PublishRequest.builder()
            .topicArn(properties.topicArn())
            .message(serialize(event))
            .messageAttributes(Map.of(
                "schemaVersion", MessageAttributeValue.builder()
                    .dataType("String").stringValue(SCHEMA_VERSION).build(),
                "eventType", MessageAttributeValue.builder()
                    .dataType("String").stringValue(event.type()).build()))
            .build();
        try {
            snsClient.publish(request);
        } catch (RuntimeException e) {
            throw new TechnicalException(
                ErrorCatalog.UNEXPECTED_ERROR.code(), "Notification publish failed", e);
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
