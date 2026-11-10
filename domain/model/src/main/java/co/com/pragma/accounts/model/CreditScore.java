package co.com.pragma.accounts.model;

import lombok.Builder;

/**
 * Resultado de una consulta a un servicio externo de score crediticio.
 * Modelo de dominio: no expone tipos de transporte del adapter.
 */
@Builder
public record CreditScore(String accountId, int score, String band) {
}
