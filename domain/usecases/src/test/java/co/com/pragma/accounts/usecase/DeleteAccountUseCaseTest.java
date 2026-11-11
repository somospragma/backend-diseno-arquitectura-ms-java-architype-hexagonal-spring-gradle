package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeleteAccountUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private DeleteAccountUseCase useCase;

    @Test
    void shouldDeleteWhenExists() {
        when(accountGateway.existsById("acc-1")).thenReturn(true);

        useCase.execute("acc-1");

        verify(accountGateway).deleteById("acc-1");
    }

    @Test
    void shouldThrowAndNotDeleteWhenMissing() {
        when(accountGateway.existsById("missing")).thenReturn(false);

        assertThrows(AccountNotFoundException.class, () -> useCase.execute("missing"));
        verify(accountGateway, never()).deleteById("missing");
    }
}
