package co.com.pragma.accounts.infrastructure.kafka;

import java.util.function.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Procesador de negocio de un registro Kafka. Aqui iria la deserializacion
 * (validando el esquema versionado) y la invocacion del use case. En el
 * arquetipo solo registra el registro como ejemplo. Version imperativa.
 */
@Component
public class KafkaRecordProcessor implements Consumer<ConsumerRecord<String, String>> {

    private static final Logger log = LoggerFactory.getLogger(KafkaRecordProcessor.class);

    @Override
    public void accept(ConsumerRecord<String, String> record) {
        log.info("Procesando registro Kafka key={} value={}", record.key(), record.value());
    }
}
