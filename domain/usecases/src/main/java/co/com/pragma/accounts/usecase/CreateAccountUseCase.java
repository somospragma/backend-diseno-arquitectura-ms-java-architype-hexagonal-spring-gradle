package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;

/**
 * Crea una cuenta con estado inicial ACTIVE. Clase Java plana (sin Spring):
 * se auto-registra como bean via UseCasesConfig (scan por regex ^.+UseCase$).
 */
@RequiredArgsConstructor
public class CreateAccountUseCase {

    private final IAccountGateway accountGateway;

    public Account execute(Account account) {
        account.setStatus("ACTIVE");
        account.setCreatedAt(LocalDateTime.now());
        return accountGateway.save(account);
    }
}
