package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Consulta de cuentas. Lanza AccountNotFoundException (negocio -> 404) cuando
 * el id no existe. El listado es paginado (page/size) para no exponer full scans.
 */
@RequiredArgsConstructor
public class GetAccountUseCase {

    private final IAccountGateway accountGateway;

    public Account execute(String id) {
        return accountGateway.findById(id)
            .orElseThrow(() -> new AccountNotFoundException(id));
    }

    public List<Account> findAll(int page, int size) {
        return accountGateway.findAll(page, size);
    }
}
