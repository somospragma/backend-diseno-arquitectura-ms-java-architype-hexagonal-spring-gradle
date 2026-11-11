package co.com.pragma.accounts.infrastructure.sns;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.AccountEvent;
import co.com.pragma.accounts.model.exception.TechnicalException;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

class SnsNotificationPublisherTest {

    private final SnsClient sns = mock(SnsClient.class);
    private final SnsProperties props = new SnsProperties("arn:aws:sns:us-east-1:1:topic", "us-east-1");
    private final SnsNotificationPublisher publisher = new SnsNotificationPublisher(sns, props);

    private AccountEvent event() {
        return AccountEvent.builder().accountId("acc-1").eventId("e-1")
            .type("ACCOUNT_CREATED").occurredAt(Instant.now()).build();
    }

    @Test
    void shouldPublishNotification() {
        when(sns.publish(any(PublishRequest.class)))
            .thenReturn(PublishResponse.builder().messageId("m-1").build());

        assertThatCode(() -> publisher.notify(event())).doesNotThrowAnyException();
    }

    @Test
    void shouldMapFailureToTechnicalException() {
        when(sns.publish(any(PublishRequest.class)))
            .thenThrow(new RuntimeException("sns down"));

        assertThatThrownBy(() -> publisher.notify(event()))
            .isInstanceOf(TechnicalException.class);
    }
}
