package co.com.pragma.accounts.model;

import co.com.pragma.accounts.model.exception.BusinessException;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import java.util.Map;

/**
 * Error de negocio: la cuenta solicitada no existe. Mapea a 404.
 */
public class AccountNotFoundException extends BusinessException {

    public AccountNotFoundException(String accountId) {
        super(
            ErrorCatalog.ACCOUNT_NOT_FOUND.code(),
            "Account not found: " + accountId,
            Map.of("accountId", accountId));
    }
}
