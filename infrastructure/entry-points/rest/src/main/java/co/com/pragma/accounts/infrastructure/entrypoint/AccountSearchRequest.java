package co.com.pragma.accounts.infrastructure.entrypoint;

/**
 * Cuerpo de la busqueda (HTTP QUERY): criterios opcionales. Un campo nulo no
 * filtra. QUERY es un metodo seguro e idempotente con body (RFC draft).
 */
public record AccountSearchRequest(
    String status,
    String accountType,
    String holderName
) {}
