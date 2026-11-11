package co.com.pragma.accounts.infrastructure.client;

import co.com.pragma.accounts.model.CreditScore;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CreditScoreMapper {
    CreditScore toModel(CreditScoreDto dto);
}
