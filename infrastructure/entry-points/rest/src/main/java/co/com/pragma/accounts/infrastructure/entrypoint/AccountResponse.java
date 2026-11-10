package co.com.pragma.accounts.infrastructure.entrypoint;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountResponse(
    String id,
    String holderName,
    String email,
    String accountType,
    BigDecimal balance,
    String status,
    LocalDateTime createdAt
) {}
