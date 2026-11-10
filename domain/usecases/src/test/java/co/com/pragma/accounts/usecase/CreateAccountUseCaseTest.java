package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateAccountUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private CreateAccountUseCase useCase;

    @Test
    void shouldSetActiveStatusAndCreatedAtBeforeSaving() {
        Account input = Account.builder()
            .holderName("Jane Doe")
            .email("jane@example.com")
            .accountType("SAVINGS")
            .balance(BigDecimal.ZERO)
            .build();
        when(accountGateway.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account result = useCase.execute(input);

        assertEquals("ACTIVE", result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(accountGateway).save(input);
    }
}
