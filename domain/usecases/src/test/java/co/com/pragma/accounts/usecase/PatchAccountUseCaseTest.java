package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PatchAccountUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private PatchAccountUseCase useCase;

    @Test
    void shouldApplyOnlyNonNullFields() {
        Account existing = Account.builder()
            .id("acc-1").holderName("Old").email("old@x.com")
            .accountType("SAVINGS").status("ACTIVE").balance(BigDecimal.TEN)
            .build();
        when(accountGateway.findById("acc-1")).thenReturn(Optional.of(existing));
        when(accountGateway.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        Account patch = Account.builder().status("FROZEN").balance(BigDecimal.ONE).build();
        Account result = useCase.execute("acc-1", patch);

        assertEquals("Old", result.getHolderName());
        assertEquals("FROZEN", result.getStatus());
        assertEquals(BigDecimal.ONE, result.getBalance());
    }

    @Test
    void shouldThrowNotFoundWhenMissing() {
        when(accountGateway.findById("missing")).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
            () -> useCase.execute("missing", Account.builder().build()));
    }
}
