package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountSearchCriteria;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Busqueda de cuentas por criterios (HTTP QUERY: busqueda segura e idempotente
 * con body). Aplica un tope de resultados para nunca devolver colecciones
 * ilimitadas (API-O-005).
 */
@RequiredArgsConstructor
public class SearchAccountsUseCase {

    private final IAccountGateway accountGateway;

    public List<Account> execute(AccountSearchCriteria criteria, int limit) {
        return accountGateway.search(criteria, limit);
    }
}
