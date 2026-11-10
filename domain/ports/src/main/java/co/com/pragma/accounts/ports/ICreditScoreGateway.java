package co.com.pragma.accounts.ports;

import co.com.pragma.accounts.model.CreditScore;

/**
 * Puerto de salida (driven) para consultar el score crediticio en un servicio
 * externo. Retorno plano (imperativo). El adapter aplica resiliencia
 * (timeout + retry + circuit breaker + bulkhead).
 */
public interface ICreditScoreGateway {

    CreditScore getScore(String accountId);
}
