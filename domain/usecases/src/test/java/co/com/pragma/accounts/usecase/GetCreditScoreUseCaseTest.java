package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.model.CreditScore;
import co.com.pragma.accounts.ports.IAccountGateway;
import co.com.pragma.accounts.ports.ICreditScoreGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetCreditScoreUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @Mock
    private ICreditScoreGateway creditScoreGateway;

    @InjectMocks
    private GetCreditScoreUseCase useCase;

    @Test
    void shouldReturnScoreForExistingAccount() {
        when(accountGateway.existsById("acc-1")).thenReturn(true);
        when(creditScoreGateway.getScore("acc-1"))
            .thenReturn(new CreditScore("acc-1", 720, "GOOD"));

        CreditScore result = useCase.execute("acc-1");

        assertEquals(720, result.score());
        assertEquals("GOOD", result.band());
    }

    @Test
    void shouldThrowNotFoundAndSkipProviderWhenAccountMissing() {
        when(accountGateway.existsById("missing")).thenReturn(false);

        assertThrows(AccountNotFoundException.class, () -> useCase.execute("missing"));
        verify(creditScoreGateway, never()).getScore("missing");
    }
}
