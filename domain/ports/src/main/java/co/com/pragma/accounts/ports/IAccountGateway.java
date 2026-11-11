package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountSearchCriteria;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida (driven) para la persistencia de cuentas. Tipos de retorno
 * planos (imperativo): nunca Mono/Flux. La paginacion se hace con offset/limit
 * y el adapter aplica un tope maximo para evitar full scans sin control.
 */
public interface IAccountGateway {

    Account save(Account account);

    Optional<Account> findById(String id);

    List<Account> findAll(int page, int size);

    void deleteById(String id);

    boolean existsById(String id);

    /** Busqueda por criterios (usada por el metodo HTTP QUERY), con tope. */
    List<Account> search(AccountSearchCriteria criteria, int limit);
}
