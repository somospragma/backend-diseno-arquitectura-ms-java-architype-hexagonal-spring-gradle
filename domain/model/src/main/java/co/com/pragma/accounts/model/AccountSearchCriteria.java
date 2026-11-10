package co.com.pragma.accounts.model;

import lombok.Builder;

/**
 * Criterios de busqueda de cuentas. Modelo de dominio usado por el metodo HTTP
 * QUERY (busqueda con body). Los campos nulos no filtran (busqueda por ejemplo).
 */
@Builder
public record AccountSearchCriteria(String status, String accountType, String holderName) {
}
