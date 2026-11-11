package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import lombok.RequiredArgsConstructor;

/**
 * Reemplazo total de una cuenta existente (HTTP PUT). Idempotente: aplicar la
 * misma peticion N veces deja el mismo estado. Falla con 404 si no existe.
 */
@RequiredArgsConstructor
public class UpdateAccountUseCase {

    private final IAccountGateway accountGateway;

    public Account execute(String id, Account replacement) {
        Account existing = accountGateway.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
        replacement.setId(existing.getId());
        replacement.setCreatedAt(existing.getCreatedAt());
        return accountGateway.save(replacement);
    }
}
