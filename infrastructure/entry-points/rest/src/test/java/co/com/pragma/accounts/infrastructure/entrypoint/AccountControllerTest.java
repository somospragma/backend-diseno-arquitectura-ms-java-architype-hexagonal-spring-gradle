package co.com.pragma.accounts.infrastructure.entrypoint;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.AccountNotFoundException;
import co.com.pragma.accounts.model.AccountSearchCriteria;
import co.com.pragma.accounts.model.CreditScore;
import co.com.pragma.accounts.usecase.CreateAccountUseCase;
import co.com.pragma.accounts.usecase.DeleteAccountUseCase;
import co.com.pragma.accounts.usecase.GetAccountUseCase;
import co.com.pragma.accounts.usecase.GetCreditScoreUseCase;
import co.com.pragma.accounts.usecase.PatchAccountUseCase;
import co.com.pragma.accounts.usecase.SearchAccountsUseCase;
import co.com.pragma.accounts.usecase.UpdateAccountUseCase;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpMethod;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Test del controller REST con MockMvc (@WebMvcTest). Los use cases son mocks
 * (@MockitoBean); el mapper real (MapStruct) y el ApiExceptionHandler se importan
 * para ejercitar el mapeo y el formato de error RFC 7807 de punta a punta.
 */
@WebMvcTest
@Import({AccountController.class, AccountQueryRouter.class, AccountRestMapperImpl.class,
    ApiExceptionHandler.class})
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CreateAccountUseCase createAccountUseCase;

    @MockitoBean
    private GetAccountUseCase getAccountUseCase;

    @MockitoBean
    private UpdateAccountUseCase updateAccountUseCase;

    @MockitoBean
    private PatchAccountUseCase patchAccountUseCase;

    @MockitoBean
    private DeleteAccountUseCase deleteAccountUseCase;

    @MockitoBean
    private SearchAccountsUseCase searchAccountsUseCase;

    @MockitoBean
    private GetCreditScoreUseCase getCreditScoreUseCase;

    private Account account(String id) {
        return Account.builder()
            .id(id)
            .holderName("Jane Doe")
            .email("jane@example.com")
            .accountType("SAVINGS")
            .balance(BigDecimal.TEN)
            .status("ACTIVE")
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Test
    void shouldCreateAccountReturning201AndMaskedEmail() throws Exception {
        when(createAccountUseCase.execute(any(Account.class))).thenReturn(account("acc-1"));

        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AccountRequest("Jane Doe", "jane@example.com", "SAVINGS", BigDecimal.ZERO))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("acc-1"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            .andExpect(jsonPath("$.email").value("****@example.com"));
    }

    @Test
    void shouldReturn400OnInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AccountRequest("", "not-an-email", "", null))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.code").value("ACC-400-001"));
    }

    @Test
    void shouldReturnAccountById() throws Exception {
        when(getAccountUseCase.execute("acc-1")).thenReturn(account("acc-1"));

        mockMvc.perform(get("/api/v1/accounts/{id}", "acc-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.holderName").value("Jane Doe"));
    }

    @Test
    void shouldReturn404ProblemDetailWhenNotFound() throws Exception {
        when(getAccountUseCase.execute("missing"))
            .thenThrow(new AccountNotFoundException("missing"));

        mockMvc.perform(get("/api/v1/accounts/{id}", "missing"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.code").value("ACC-404-001"))
            .andExpect(jsonPath("$.metadata.accountId").value("missing"));
    }

    @Test
    void shouldListWithPaginationCap() throws Exception {
        when(getAccountUseCase.findAll(eq(0), anyInt())).thenReturn(List.of(account("acc-1")));

        mockMvc.perform(get("/api/v1/accounts").param("size", "9999"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("acc-1"));

        // El tope duro (100) se aplica: nunca se pide 9999 al use case.
        verify(getAccountUseCase).findAll(0, 100);
    }

    @Test
    void shouldReturnCreditScore() throws Exception {
        when(getCreditScoreUseCase.execute("acc-1"))
            .thenReturn(new CreditScore("acc-1", 700, "GOOD"));

        mockMvc.perform(get("/api/v1/accounts/{id}/credit-score", "acc-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(700));
    }

    @Test
    void shouldDeleteReturning204() throws Exception {
        mockMvc.perform(delete("/api/v1/accounts/{id}", "acc-1"))
            .andExpect(status().isNoContent());
        verify(deleteAccountUseCase).execute("acc-1");
    }

    @Test
    void shouldReplaceAccountWithPut() throws Exception {
        when(updateAccountUseCase.execute(eq("acc-1"), any(Account.class)))
            .thenReturn(account("acc-1"));

        mockMvc.perform(put("/api/v1/accounts/{id}", "acc-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(
                    new AccountRequest("Jane Doe", "jane@example.com", "SAVINGS", BigDecimal.TEN))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("acc-1"));
        verify(updateAccountUseCase).execute(eq("acc-1"), any(Account.class));
    }

    @Test
    void shouldPatchAccount() throws Exception {
        Account patched = account("acc-1");
        patched.setStatus("FROZEN");
        when(patchAccountUseCase.execute(eq("acc-1"), any(Account.class))).thenReturn(patched);

        mockMvc.perform(patch("/api/v1/accounts/{id}", "acc-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"FROZEN\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("FROZEN"));
    }

    @Test
    void shouldSearchWithQueryMethod() throws Exception {
        when(searchAccountsUseCase.execute(any(AccountSearchCriteria.class), anyInt()))
            .thenReturn(List.of(account("acc-1")));

        mockMvc.perform(request(HttpMethod.valueOf("QUERY"), "/api/v1/accounts/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"ACTIVE\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("acc-1"));
    }

    @Test
    void shouldReturn500GenericOnTechnicalError() throws Exception {
        doThrow(new RuntimeException("db down at internal-host:5432"))
            .when(getAccountUseCase).execute("boom");

        mockMvc.perform(get("/api/v1/accounts/{id}", "boom"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.code").value("ACC-500-001"))
            .andExpect(header().doesNotExist("X-Internal-Detail"));
    }
}
