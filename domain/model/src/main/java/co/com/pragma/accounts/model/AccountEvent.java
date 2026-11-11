package co.com.pragma.accounts.model;

import java.time.Instant;
import lombok.Builder;

/**
 * Evento del historial de una cuenta (caso tipico de acceso por clave en un
 * store key-value/wide-column como DynamoDB: PK=accountId, SK=timestamp).
 */
@Builder
public record AccountEvent(String accountId, String eventId, String type, Instant occurredAt) {
}
