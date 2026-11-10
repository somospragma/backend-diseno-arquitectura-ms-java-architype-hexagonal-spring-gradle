package co.com.pragma.accounts.infrastructure.sqs;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.sqs.model.Message;

class AccountMessageProcessorTest {

    @Test
    void shouldProcessMessageWithoutError() {
        AccountMessageProcessor processor = new AccountMessageProcessor();
        Message message = Message.builder().messageId("m-1").body("{\"event\":\"created\"}").build();

        assertThatCode(() -> processor.accept(message)).doesNotThrowAnyException();
    }
}
