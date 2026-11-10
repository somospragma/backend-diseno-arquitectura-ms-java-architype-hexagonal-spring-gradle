package co.com.pragma.accounts.infrastructure.persistence;

import co.com.pragma.accounts.model.Account;
import org.mapstruct.Mapper;

/**
 * Mapeo dominio &lt;-&gt; entidad JPA con MapStruct. Nunca mapeo manual.
 */
@Mapper(componentModel = "spring")
public interface AccountEntityMapper {

    AccountEntity toEntity(Account account);

    Account toModel(AccountEntity entity);
}
