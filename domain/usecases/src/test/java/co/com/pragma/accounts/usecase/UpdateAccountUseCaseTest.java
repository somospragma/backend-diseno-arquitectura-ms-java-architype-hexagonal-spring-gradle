package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UpdateAccountUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private UpdateAccountUseCase useCase;

    @Test
    void shouldReplacePreservingIdAndCreatedAt() {
        LocalDateTime created = LocalDateTime.now().minusDays(3);
        Account existing = Account.builder().id("acc-1").createdAt(created).holderName("Old").build();
        when(accountGateway.findById("acc-1")).thenReturn(Optional.of(existing));
        when(accountGateway.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account replacement = Account.builder().holderName("New").status("FROZEN").build();
        Account result = useCase.execute("acc-1", replacement);

        assertEquals("acc-1", result.getId());
        assertEquals(created, result.getCreatedAt());
        assertEquals("New", result.getHolderName());
    }

    @Test
    void shouldThrowNotFoundWhenMissing() {
        when(accountGateway.findById("missing")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
            () -> useCase.execute("missing", Account.builder().build()));
    }
}
