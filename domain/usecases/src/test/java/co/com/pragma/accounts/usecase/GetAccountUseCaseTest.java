package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetAccountUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private GetAccountUseCase useCase;

    @Test
    void shouldReturnAccountWhenFound() {
        Account account = Account.builder().id("acc-1").holderName("Jane").build();
        when(accountGateway.findById("acc-1")).thenReturn(Optional.of(account));

        Account result = useCase.execute("acc-1");

        assertEquals("acc-1", result.getId());
    }

    @Test
    void shouldThrowNotFoundWhenMissing() {
        when(accountGateway.findById("missing")).thenReturn(Optional.empty());

        AccountNotFoundException ex =
            assertThrows(AccountNotFoundException.class, () -> useCase.execute("missing"));
        assertEquals("ACC-404-001", ex.code());
    }

    @Test
    void shouldDelegatePaginationToGateway() {
        when(accountGateway.findAll(1, 25))
            .thenReturn(List.of(Account.builder().id("acc-2").build()));

        List<Account> result = useCase.findAll(1, 25);

        assertEquals(1, result.size());
    }
}
