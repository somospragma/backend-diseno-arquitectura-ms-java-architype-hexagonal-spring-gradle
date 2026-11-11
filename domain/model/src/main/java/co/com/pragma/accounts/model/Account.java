package co.com.pragma.accounts.model;

import co.com.pragma.accounts.model.sensitive.Sensitive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {
    private String id;
    private String holderName;

    /** Dato sensible (PII): se enmascara en logs y respuestas. */
    @Sensitive(value = Sensitive.Classification.PII, strategy = Sensitive.Strategy.EMAIL)
    private String email;

    private String accountType;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;
}
