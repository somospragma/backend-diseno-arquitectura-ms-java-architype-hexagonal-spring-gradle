package co.com.pragma.accounts.infrastructure.kafka;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.model.exception.TechnicalException;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

class KafkaEventStreamPublisherTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, String> template = mock(KafkaTemplate.class);
    private final KafkaEventStreamPublisher publisher =
        new KafkaEventStreamPublisher(template, "accounts.events");

    private AccountEvent event() {
        return AccountEvent.builder().accountId("acc-1").eventId("e-1")
            .type("ACCOUNT_CREATED").occurredAt(Instant.now()).build();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldPublishToKafka() {
        SendResult<String, String> result = mock(SendResult.class);
        when(template.send(any(ProducerRecord.class)))
            .thenReturn(CompletableFuture.completedFuture(result));

        assertThatCode(() -> publisher.publish(event())).doesNotThrowAnyException();
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldMapFailureToTechnicalException() {
        when(template.send(any(ProducerRecord.class)))
            .thenReturn(CompletableFuture.failedFuture(new RuntimeException("kafka down")));

        assertThatThrownBy(() -> publisher.publish(event()))
            .isInstanceOf(TechnicalException.class);
    }
}
