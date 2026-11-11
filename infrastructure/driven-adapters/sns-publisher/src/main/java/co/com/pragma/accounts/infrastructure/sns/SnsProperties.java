package co.com.pragma.accounts.infrastructure.sns;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Config del publisher SNS. Se activa con sns.enabled=true. */
@ConfigurationProperties(prefix = "sns")
public record SnsProperties(String topicArn, String region) {
}
