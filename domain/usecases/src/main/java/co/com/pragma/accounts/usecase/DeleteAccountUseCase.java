package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import lombok.RequiredArgsConstructor;

/**
 * Elimina una cuenta. Verifica existencia primero para responder 404 (negocio)
 * en lugar de un borrado silencioso de un id inexistente.
 */
@RequiredArgsConstructor
public class DeleteAccountUseCase {

    private final IAccountGateway accountGateway;

    public void execute(String id) {
        if (!accountGateway.existsById(id)) {
            throw new AccountNotFoundException(id);
        }
        accountGateway.deleteById(id);
    }
}
