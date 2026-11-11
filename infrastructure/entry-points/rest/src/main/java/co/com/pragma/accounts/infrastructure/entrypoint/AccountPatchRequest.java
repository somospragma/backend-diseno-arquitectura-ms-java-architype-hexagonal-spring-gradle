package co.com.pragma.accounts.infrastructure.entrypoint;

import jakarta.validation.constraints.Email;
import java.math.BigDecimal;

/**
 * Cuerpo de la actualizacion parcial (HTTP PATCH): todos los campos son
 * opcionales. Solo el email se valida de formato cuando viene presente.
 */
public record AccountPatchRequest(
    String holderName,
    @Email String email,
    String accountType,
    BigDecimal balance,
    String status
) {}
