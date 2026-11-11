package co.com.pragma.accounts.infrastructure.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import co.com.pragma.accounts.model.Account;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

/**
 * Test de integracion del adapter JPA sobre H2 embebido (@DataJpaTest). Verifica
 * el mapeo dominio&lt;-&gt;entidad, la generacion de id, la paginacion con tope y el
 * borrado. No usa Docker/Testcontainers (H2 en memoria).
 */
@DataJpaTest
@Import({AccountJpaAdapter.class, AccountEntityMapperImpl.class})
class AccountJpaAdapterTest {

    @Autowired
    private AccountJpaAdapter adapter;

    private Account sample(String name) {
        return Account.builder()
            .holderName(name)
            .email(name + "@example.com")
            .accountType("SAVINGS")
            .balance(BigDecimal.TEN)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void shouldGenerateIdOnSaveAndFindById() {
        Account saved = adapter.save(sample("Jane"));

        assertThat(saved.getId()).isNotBlank();
        Optional<Account> found = adapter.findById(saved.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getHolderName()).isEqualTo("Jane");
    }

    @Test
    void shouldRespectProvidedId() {
        Account withId = sample("John");
        withId.setId("fixed-id");

        Account saved = adapter.save(withId);

        assertThat(saved.getId()).isEqualTo("fixed-id");
        assertThat(adapter.existsById("fixed-id")).isTrue();
    }

    @Test
    void shouldPaginateFindAll() {
        for (int i = 0; i < 5; i++) {
            adapter.save(sample("User" + i));
        }

        List<Account> firstPage = adapter.findAll(0, 2);
        assertThat(firstPage).hasSize(2);
    }

    @Test
    void shouldDeleteById() {
        Account saved = adapter.save(sample("Temp"));

        adapter.deleteById(saved.getId());

        assertThat(adapter.existsById(saved.getId())).isFalse();
    }

    @Test
    void shouldSearchByCriteriaWithLimit() {
        Account frozen = sample("Frozen");
        frozen.setStatus("FROZEN");
        adapter.save(frozen);
        adapter.save(sample("Active1"));
        adapter.save(sample("Active2"));

        List<Account> frozenOnly = adapter.search(
            co.com.pragma.accounts.model.AccountSearchCriteria.builder()
                .status("FROZEN").build(),
            10);
        assertThat(frozenOnly).hasSize(1);
        assertThat(frozenOnly.get(0).getHolderName()).isEqualTo("Frozen");

        List<Account> capped = adapter.search(
            co.com.pragma.accounts.model.AccountSearchCriteria.builder().build(), 1);
        assertThat(capped).hasSize(1);
    }
}
