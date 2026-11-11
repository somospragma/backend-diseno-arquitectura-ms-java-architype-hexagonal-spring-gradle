package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.Account;
import java.util.List;

/**
 * Puerto de salida hacia OTRO microservicio de cuentas via gRPC (comunicacion
 * sincrona interna, MSG-BP-006). Retorna el modelo de dominio; el transporte
 * gRPC queda encapsulado en el adapter. Version imperativa: tipos planos.
 */
public interface IExternalAccountGateway {

    Account getAccount(String id);

    List<Account> listAccounts(int limit);
}
