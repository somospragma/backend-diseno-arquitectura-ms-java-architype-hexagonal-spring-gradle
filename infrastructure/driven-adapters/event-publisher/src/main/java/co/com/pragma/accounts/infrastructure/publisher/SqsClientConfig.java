package co.com.pragma.accounts.infrastructure.publisher;

import java.net.URI;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * Cliente SQS sincrono, activado solo si sqs.enabled=true (para no exigir SQS al
 * arrancar el showcase por defecto). Reutilizable por publisher y consumer.
 */
@Configuration
@ConditionalOnProperty(prefix = "sqs", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(EventPublisherProperties.class)
public class SqsClientConfig {

    @Bean
    @ConditionalOnMissingBean
    public SqsClient sqsClient(@Value("${sqs.endpoint:}") String endpoint) {
        var builder = SqsClient.builder();
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }
}
