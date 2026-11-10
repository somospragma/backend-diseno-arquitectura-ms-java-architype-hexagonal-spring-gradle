package co.com.pragma.accounts.infrastructure.persistence;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repositorio Spring Data JPA para AccountEntity. Provee paginacion via
 * PagingAndSortingRepository (findAll(Pageable)) — nunca full scan sin tope.
 */
public interface AccountJpaRepository extends JpaRepository<AccountEntity, String> {

    /**
     * Busqueda por criterios (HTTP QUERY): un parametro nulo NO filtra
     * (busqueda por ejemplo). El tope se aplica via Pageable.
     */
    @Query("SELECT a FROM AccountEntity a WHERE "
        + "(:status IS NULL OR a.status = :status) AND "
        + "(:accountType IS NULL OR a.accountType = :accountType) AND "
        + "(:holderName IS NULL OR a.holderName = :holderName)")
    List<AccountEntity> search(
        @Param("status") String status,
        @Param("accountType") String accountType,
        @Param("holderName") String holderName,
        Pageable pageable);
}
