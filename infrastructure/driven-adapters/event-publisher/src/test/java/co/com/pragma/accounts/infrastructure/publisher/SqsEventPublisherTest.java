package co.com.pragma.accounts.infrastructure.publisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.model.exception.TechnicalException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

class SqsEventPublisherTest {

    private AccountEvent event() {
        return AccountEvent.builder()
            .accountId("acc-1").eventId("ev-1").type("CREATED")
            .occurredAt(Instant.parse("2026-01-01T00:00:00Z")).build();
    }

    private EventPublisherProperties props() {
        EventPublisherProperties p = new EventPublisherProperties();
        p.setQueueUrl("http://localhost/q");
        return p;
    }

    @Test
    void shouldPublishWithVersionedSchema() {
        SqsClient sqs = mock(SqsClient.class);
        ArgumentCaptor<SendMessageRequest> captor = ArgumentCaptor.forClass(SendMessageRequest.class);
        when(sqs.sendMessage(captor.capture())).thenReturn(SendMessageResponse.builder().build());

        new SqsEventPublisher(sqs, props()).publish(event());

        SendMessageRequest req = captor.getValue();
        assertThat(req.messageBody()).contains("\"schemaVersion\":\"1\"");
        assertThat(req.messageAttributes()).containsKey("schemaVersion");
        assertThat(req.messageAttributes()).containsKey("eventType");
    }

    @Test
    void shouldMapErrorToTechnicalException() {
        SqsClient sqs = mock(SqsClient.class);
        when(sqs.sendMessage(any(SendMessageRequest.class)))
            .thenThrow(new RuntimeException("sqs down"));

        assertThatThrownBy(() -> new SqsEventPublisher(sqs, props()).publish(event()))
            .isInstanceOf(TechnicalException.class);
    }
}
