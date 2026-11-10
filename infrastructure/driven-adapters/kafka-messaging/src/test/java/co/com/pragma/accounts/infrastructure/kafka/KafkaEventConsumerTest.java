package co.com.pragma.accounts.infrastructure.kafka;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import co.com.pragma.accounts.ports.IIdempotencyStore;
import java.time.Duration;
import java.util.function.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.Acknowledgment;

class KafkaEventConsumerTest {

    private static IIdempotencyStore inMemoryStore() {
        java.util.Set<String> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return (key, ttl) -> seen.add(key);
    }

    private ConsumerRecord<String, String> record(long offset) {
        return new ConsumerRecord<>("accounts.events", 0, offset, "acc-1", "{\"e\":1}");
    }

    @Test
    void shouldAckAfterSuccessfulProcessing() {
        Acknowledgment ack = mock(Acknowledgment.class);
        Consumer<ConsumerRecord<String, String>> processor = rec -> { };
        KafkaEventConsumer consumer = new KafkaEventConsumer(processor, inMemoryStore());

        consumer.handleRecordIsolated(record(1L), ack);

        verify(ack).acknowledge();
    }

    @Test
    void shouldNotAckWhenProcessingFails() {
        Acknowledgment ack = mock(Acknowledgment.class);
        Consumer<ConsumerRecord<String, String>> processor = rec -> {
            throw new RuntimeException("boom");
        };
        KafkaEventConsumer consumer = new KafkaEventConsumer(processor, inMemoryStore());

        consumer.handleRecordIsolated(record(2L), ack);

        verify(ack, never()).acknowledge();
    }

    @Test
    void shouldSkipDuplicateButAck() {
        Acknowledgment ack = mock(Acknowledgment.class);
        int[] processed = {0};
        Consumer<ConsumerRecord<String, String>> processor = rec -> processed[0]++;
        IIdempotencyStore store = inMemoryStore();
        KafkaEventConsumer consumer = new KafkaEventConsumer(processor, store);

        consumer.handleRecordIsolated(record(3L), ack);
        consumer.handleRecordIsolated(record(3L), ack);

        org.junit.jupiter.api.Assertions.assertEquals(1, processed[0]);
        verify(ack, times(2)).acknowledge(); // ambos hacen ack; el duplicado no reprocesa
    }

    @Test
    void onMessageDelegatesToHandler() {
        Acknowledgment ack = mock(Acknowledgment.class);
        Consumer<ConsumerRecord<String, String>> processor = rec -> { };
        KafkaEventConsumer consumer = new KafkaEventConsumer(processor, inMemoryStore());

        consumer.onMessage(record(5L), ack);

        verify(ack).acknowledge();
    }

    @Test
    void dedupeTtlIsUsed() {
        // Verifica que el store recibe el TTL de deduplicacion (6h).
        Acknowledgment ack = mock(Acknowledgment.class);
        Duration[] captured = new Duration[1];
        IIdempotencyStore store = (key, ttl) -> {
            captured[0] = ttl;
            return true;
        };
        KafkaEventConsumer consumer = new KafkaEventConsumer(rec -> { }, store);

        consumer.handleRecordIsolated(record(9L), ack);

        org.junit.jupiter.api.Assertions.assertEquals(Duration.ofHours(6), captured[0]);
    }
}
