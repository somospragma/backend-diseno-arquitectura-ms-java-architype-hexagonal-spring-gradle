package co.com.pragma.accounts.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountSearchCriteria;
import co.com.pragma.accounts.ports.IAccountGateway;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SearchAccountsUseCaseTest {

    @Mock
    private IAccountGateway accountGateway;

    @InjectMocks
    private SearchAccountsUseCase useCase;

    @Test
    void shouldDelegateSearchToGateway() {
        AccountSearchCriteria criteria =
            AccountSearchCriteria.builder().status("ACTIVE").build();
        when(accountGateway.search(criteria, 50))
            .thenReturn(List.of(Account.builder().id("acc-1").status("ACTIVE").build()));

        List<Account> result = useCase.execute(criteria, 50);

        assertEquals(1, result.size());
        assertEquals("acc-1", result.get(0).getId());
    }
}
