package co.com.pragma.accounts.usecase;

import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.model.CreditScore;
import co.com.pragma.accounts.ports.IAccountGateway;
import co.com.pragma.accounts.ports.ICreditScoreGateway;
import lombok.RequiredArgsConstructor;

/**
 * Consulta el score crediticio de una cuenta existente. Valida que la cuenta
 * exista (404 de negocio) antes de llamar al servicio externo resiliente.
 */
@RequiredArgsConstructor
public class GetCreditScoreUseCase {

    private final IAccountGateway accountGateway;
    private final ICreditScoreGateway creditScoreGateway;

    public CreditScore execute(String accountId) {
        if (!accountGateway.existsById(accountId)) {
            throw new AccountNotFoundException(accountId);
        }
        return creditScoreGateway.getScore(accountId);
    }
}
