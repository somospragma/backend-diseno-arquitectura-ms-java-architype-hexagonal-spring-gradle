package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;

/**
 * Actualizacion parcial de una cuenta (HTTP PATCH): solo aplica los campos no
 * nulos del parche sobre la cuenta existente. Falla con 404 si no existe.
 */
@RequiredArgsConstructor
public class PatchAccountUseCase {

    private final IAccountGateway accountGateway;

    public Account execute(String id, Account patch) {
        Account existing = accountGateway.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
        applyIfPresent(patch.getHolderName(), existing::setHolderName);
        applyIfPresent(patch.getEmail(), existing::setEmail);
        applyIfPresent(patch.getAccountType(), existing::setAccountType);
        applyIfPresent(patch.getStatus(), existing::setStatus);
        if (patch.getBalance() != null) {
            existing.setBalance(patch.getBalance());
        }
        return accountGateway.save(existing);
    }

    private void applyIfPresent(String value, java.util.function.Consumer<String> setter) {
        if (value != null && !value.isBlank()) {
            setter.accept(value);
        }
    }
}
