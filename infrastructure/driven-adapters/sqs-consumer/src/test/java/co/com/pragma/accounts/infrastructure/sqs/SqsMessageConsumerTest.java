package co.com.pragma.accounts.infrastructure.sqs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.ports.IIdempotencyStore;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.DeleteMessageResponse;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageResponse;

class SqsMessageConsumerTest {

    private Message msg(String id, String body) {
        return Message.builder().messageId(id).receiptHandle("rh-" + id).body(body).build();
    }

    /** Store en memoria: markIfFirst devuelve true solo la primera vez por clave. */
    private static IIdempotencyStore inMemoryStore() {
        java.util.Set<String> seen = java.util.concurrent.ConcurrentHashMap.newKeySet();
        return (key, ttl) -> seen.add(key);
    }

    @Test
    void poisonMessageMustNotStopProcessingOfOthers() {
        SqsClient sqs = mock(SqsClient.class);
        ConcurrentLinkedQueue<String> deleted = new ConcurrentLinkedQueue<>();
        when(sqs.deleteMessage(any(DeleteMessageRequest.class))).thenAnswer(inv -> {
            deleted.add(((DeleteMessageRequest) inv.getArgument(0)).receiptHandle());
            return DeleteMessageResponse.builder().build();
        });

        AtomicInteger processed = new AtomicInteger();
        Consumer<Message> processor = m -> {
            if ("poison".equals(m.body())) {
                throw new RuntimeException("boom en un mensaje");
            }
            processed.incrementAndGet();
        };

        SqsMessageConsumer consumer = new SqsMessageConsumer(sqs, "queue-url", processor, inMemoryStore());

        consumer.handleMessageIsolated(msg("1", "ok"));
        consumer.handleMessageIsolated(msg("2", "poison"));
        consumer.handleMessageIsolated(msg("3", "ok"));

        assertThat(processed.get()).isEqualTo(2);
        assertThat(deleted).containsExactlyInAnyOrder("rh-1", "rh-3");
    }

    @Test
    void receiveBatchMustEmitReceivedMessages() {
        SqsClient sqs = mock(SqsClient.class);
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder()
                .messages(msg("1", "ok"), msg("2", "ok")).build());

        SqsMessageConsumer consumer = new SqsMessageConsumer(sqs, "q", m -> { }, inMemoryStore());

        assertThat(consumer.receiveBatch()).hasSize(2);
    }

    @Test
    void successfulMessageMustBeDeleted() {
        SqsClient sqs = mock(SqsClient.class);
        ConcurrentLinkedQueue<String> deleted = new ConcurrentLinkedQueue<>();
        when(sqs.deleteMessage(any(DeleteMessageRequest.class))).thenAnswer(inv -> {
            deleted.add(((DeleteMessageRequest) inv.getArgument(0)).receiptHandle());
            return DeleteMessageResponse.builder().build();
        });

        SqsMessageConsumer consumer = new SqsMessageConsumer(sqs, "q", m -> { }, inMemoryStore());

        consumer.handleMessageIsolated(msg("1", "ok"));

        assertThat(deleted).containsExactly("rh-1");
    }

    @Test
    void duplicateMessageMustBeSkippedButDeleted() {
        SqsClient sqs = mock(SqsClient.class);
        ConcurrentLinkedQueue<String> deleted = new ConcurrentLinkedQueue<>();
        when(sqs.deleteMessage(any(DeleteMessageRequest.class))).thenAnswer(inv -> {
            deleted.add(((DeleteMessageRequest) inv.getArgument(0)).receiptHandle());
            return DeleteMessageResponse.builder().build();
        });
        AtomicInteger processed = new AtomicInteger();
        IIdempotencyStore store = inMemoryStore();

        SqsMessageConsumer consumer = new SqsMessageConsumer(sqs, "q", m -> processed.incrementAndGet(), store);

        consumer.handleMessageIsolated(msg("dup", "ok"));
        consumer.handleMessageIsolated(msg("dup", "ok"));

        assertThat(processed.get()).isEqualTo(1);
        assertThat(deleted).hasSize(2);
    }

    @Test
    void startAndStopShouldManageLifecycle() {
        SqsClient sqs = mock(SqsClient.class);
        when(sqs.receiveMessage(any(ReceiveMessageRequest.class)))
            .thenReturn(ReceiveMessageResponse.builder().build());

        SqsMessageConsumer consumer = new SqsMessageConsumer(sqs, "q", m -> { }, inMemoryStore());
        consumer.start();
        consumer.stop(); // no debe lanzar
    }
}
