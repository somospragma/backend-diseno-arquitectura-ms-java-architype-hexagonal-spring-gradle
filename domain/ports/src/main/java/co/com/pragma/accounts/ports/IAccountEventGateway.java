package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.AccountEvent;
import java.util.List;

/**
 * Puerto de salida hacia el store de eventos por cuenta (DynamoDB).
 * La consulta es SIEMPRE por clave (accountId), nunca un barrido completo.
 * Version imperativa: tipos planos, sin Mono/Flux.
 */
public interface IAccountEventGateway {

    void append(AccountEvent event);

    /** Lista los eventos de una cuenta por Query sobre la PK, con tope. */
    List<AccountEvent> findByAccountId(String accountId, int limit);
}
