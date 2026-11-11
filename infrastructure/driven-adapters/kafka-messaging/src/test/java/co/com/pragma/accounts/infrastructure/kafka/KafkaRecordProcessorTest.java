package co.com.pragma.accounts.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;

class KafkaRecordProcessorTest {

    @Test
    void shouldProcessRecordWithoutError() {
        KafkaRecordProcessor processor = new KafkaRecordProcessor();
        ConsumerRecord<String, String> record =
            new ConsumerRecord<>("accounts.events", 0, 0L, "acc-1", "{\"event\":\"created\"}");

        assertThatCode(() -> processor.accept(record)).doesNotThrowAnyException();
    }
}
