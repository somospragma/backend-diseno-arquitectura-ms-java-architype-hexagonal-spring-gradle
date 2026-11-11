package co.com.pragma.accounts.infrastructure.sqs;

import co.com.pragma.accounts.ports.IIdempotencyStore;
import java.net.URI;
import java.util.function.Consumer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.Message;

/**
 * Cablea el consumidor SQS como bean gestionado y lo ARRANCA/DETIENE con el
 * ciclo de vida del contexto (SmartLifecycle). Se activa solo con
 * sqs.consumer.enabled=true, para no exigir infraestructura AWS en el arranque
 * por defecto del arquetipo.
 */
@Configuration
@ConditionalOnProperty(prefix = "sqs.consumer", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SqsConsumerProperties.class)
public class SqsConsumerConfig {

    @Bean
    @ConditionalOnMissingBean
    public SqsClient sqsConsumerClient(SqsConsumerProperties props,
                                       @Value("${sqs.endpoint:}") String endpoint) {
        var builder = SqsClient.builder();
        if (props.region() != null && !props.region().isBlank()) {
            builder.region(Region.of(props.region()));
        }
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder.build();
    }

    @Bean
    public SqsMessageConsumer sqsMessageConsumer(SqsClient sqsConsumerClient,
                                                 SqsConsumerProperties props,
                                                 Consumer<Message> processor,
                                                 IIdempotencyStore idempotencyStore) {
        return new SqsMessageConsumer(sqsConsumerClient, props.queueUrl(), processor, idempotencyStore);
    }

    /** Arranca el poll loop al iniciar el contexto y lo detiene al cerrarlo. */
    @Bean
    public SmartLifecycle sqsConsumerLifecycle(SqsMessageConsumer consumer) {
        return new SmartLifecycle() {
            private volatile boolean running;

            @Override
            public void start() {
                consumer.start();
                running = true;
            }

            @Override
            public void stop() {
                consumer.stop();
                running = false;
            }

            @Override
            public boolean isRunning() {
                return running;
            }
        };
    }
}
