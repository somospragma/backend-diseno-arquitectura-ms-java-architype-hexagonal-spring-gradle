package co.com.pragma.accounts.infrastructure.kafka;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Config del adapter Kafka imperativo (spring-kafka). Se activa con
 * kafka.enabled=true. La DLQ y la politica de reintentos del topico se definen
 * en la infraestructura del broker; el consumidor asume su existencia (MSG-O-002).
 */
@ConfigurationProperties(prefix = "kafka")
public record KafkaProperties(String bootstrapServers, String topic, String consumerGroup) {
}
