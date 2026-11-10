package co.com.pragma.accounts.infrastructure.entrypoint;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record AccountRequest(
    @NotBlank String holderName,
    @NotBlank @Email String email,
    @NotBlank String accountType,
    @NotNull BigDecimal initialBalance
) {}
