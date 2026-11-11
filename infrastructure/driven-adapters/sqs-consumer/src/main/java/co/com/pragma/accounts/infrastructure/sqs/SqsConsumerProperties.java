package co.com.pragma.accounts.infrastructure.sqs;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuracion del consumidor SQS. Se activa con sqs.consumer.enabled=true.
 * La DLQ y el maxReceiveCount se definen en la infraestructura de la cola (IaC);
 * el consumidor asume que la redrive policy existe (MSG-O-002).
 */
@ConfigurationProperties(prefix = "sqs.consumer")
public record SqsConsumerProperties(String queueUrl, String region) {
}
