package co.com.pragma.accounts.infrastructure.persistence;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountSearchCriteria;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

/**
 * Adapter de persistencia JPA que implementa el puerto IAccountGateway.
 * Asigna id (UUID) al crear si no viene, y expone la paginacion con tope
 * mediante Pageable (nunca findAll() sin limite).
 */
@Repository
@RequiredArgsConstructor
public class AccountJpaAdapter implements IAccountGateway {

    private final AccountJpaRepository repository;
    private final AccountEntityMapper mapper;

    @Override
    public Account save(Account account) {
        if (account.getId() == null || account.getId().isBlank()) {
            account.setId(UUID.randomUUID().toString());
        }
        AccountEntity saved = repository.save(mapper.toEntity(account));
        return mapper.toModel(saved);
    }

    @Override
    public Optional<Account> findById(String id) {
        return repository.findById(id).map(mapper::toModel);
    }

    @Override
    public List<Account> findAll(int page, int size) {
        return repository.findAll(PageRequest.of(page, size)).stream()
            .map(mapper::toModel)
            .toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public List<Account> search(AccountSearchCriteria criteria, int limit) {
        return repository.search(
                criteria.status(),
                criteria.accountType(),
                criteria.holderName(),
                PageRequest.of(0, Math.max(1, limit)))
            .stream()
            .map(mapper::toModel)
            .toList();
    }
}
