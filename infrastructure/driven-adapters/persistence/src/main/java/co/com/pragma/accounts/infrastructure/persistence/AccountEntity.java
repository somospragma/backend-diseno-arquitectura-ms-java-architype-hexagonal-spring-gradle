package co.com.pragma.accounts.infrastructure.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad JPA. Vive SOLO en infrastructure; el dominio nunca la conoce.
 * El mapeo entre Account (dominio) y AccountEntity lo hace MapStruct.
 */
@Entity
@Table(name = "accounts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    @Column(nullable = false, updatable = false)
    private String id;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(nullable = false)
    private String email;

    @Column(name = "account_type", nullable = false)
    private String accountType;

    @Column(nullable = false)
    private BigDecimal balance;

    @Column(nullable = false)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
