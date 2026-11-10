package co.com.pragma.accounts.infrastructure.entrypoint;

import co.com.pragma.accounts.infrastructure.helpers.masking.DataMasker;
import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountSearchCriteria;
import co.com.pragma.accounts.model.sensitive.Sensitive;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AccountRestMapper {

    @Mapping(target = "balance", source = "initialBalance")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Account toModel(AccountRequest request);

    // PATCH: mapea solo los campos del parche; id/createdAt se preservan en el use case.
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Account toModel(AccountPatchRequest request);

    AccountSearchCriteria toCriteria(AccountSearchRequest request);

    // El email (PII) se enmascara en la respuesta (DSC-O-003): nunca se expone completo.
    @Mapping(target = "email", source = "email", qualifiedByName = "maskEmail")
    AccountResponse toResponse(Account account);

    @Named("maskEmail")
    default String maskEmail(String email) {
        return DataMasker.mask(email, Sensitive.Strategy.EMAIL);
    }
}
