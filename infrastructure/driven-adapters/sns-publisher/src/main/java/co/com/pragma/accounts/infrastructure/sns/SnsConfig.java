package co.com.pragma.accounts.infrastructure.sns;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sns.SnsClient;

/** Wiring del cliente SNS sincrono. Solo activo con sns.enabled=true. */
@Configuration
@ConditionalOnProperty(prefix = "sns", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(SnsProperties.class)
public class SnsConfig {

    @Bean(destroyMethod = "close")
    public SnsClient snsClient(SnsProperties props) {
        var builder = SnsClient.builder();
        if (props.region() != null && !props.region().isBlank()) {
            builder.region(Region.of(props.region()));
        }
        return builder.build();
    }
}
