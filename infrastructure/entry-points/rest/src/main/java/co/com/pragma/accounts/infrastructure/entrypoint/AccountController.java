package co.com.pragma.accounts.infrastructure.entrypoint;

import co.com.pragma.accounts.model.Account;
import co.com.pragma.accounts.model.CreditScore;
import co.com.pragma.accounts.usecase.CreateAccountUseCase;
import co.com.pragma.accounts.usecase.DeleteAccountUseCase;
import co.com.pragma.accounts.usecase.GetAccountUseCase;
import co.com.pragma.accounts.usecase.GetCreditScoreUseCase;
import co.com.pragma.accounts.usecase.PatchAccountUseCase;
import co.com.pragma.accounts.usecase.UpdateAccountUseCase;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Entry-point REST imperativo (Spring MVC). El controller SOLO delega en los
 * use cases y mapea DTOs; no contiene logica de negocio (API-O / hexagonal).
 * Metodos y codigos HTTP correctos, paginacion con tope duro.
 */
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    /** Tope duro de tamano de pagina (API-O-005): nunca colecciones ilimitadas. */
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final CreateAccountUseCase createAccountUseCase;
    private final GetAccountUseCase getAccountUseCase;
    private final UpdateAccountUseCase updateAccountUseCase;
    private final PatchAccountUseCase patchAccountUseCase;
    private final DeleteAccountUseCase deleteAccountUseCase;
    private final GetCreditScoreUseCase getCreditScoreUseCase;
    private final AccountRestMapper accountRestMapper;

    @PostMapping
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody AccountRequest request) {
        Account created = createAccountUseCase.execute(accountRestMapper.toModel(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(accountRestMapper.toResponse(created));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> findById(@PathVariable String id) {
        Account account = getAccountUseCase.execute(id);
        return ResponseEntity.ok(accountRestMapper.toResponse(account));
    }

    @GetMapping
    public ResponseEntity<List<AccountResponse>> findAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int cappedSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int cappedPage = Math.max(page, 0);
        List<AccountResponse> body = getAccountUseCase.findAll(cappedPage, cappedSize).stream()
            .map(accountRestMapper::toResponse)
            .toList();
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}/credit-score")
    public ResponseEntity<CreditScore> creditScore(@PathVariable String id) {
        return ResponseEntity.ok(getCreditScoreUseCase.execute(id));
    }

    /** HTTP PUT: reemplazo total (idempotente). El use case preserva id/createdAt. */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponse> update(
            @PathVariable String id, @Valid @RequestBody AccountRequest request) {
        Account updated = updateAccountUseCase.execute(id, accountRestMapper.toModel(request));
        return ResponseEntity.ok(accountRestMapper.toResponse(updated));
    }

    /** HTTP PATCH: actualizacion parcial (solo campos presentes). */
    @PatchMapping("/{id}")
    public ResponseEntity<AccountResponse> patch(
            @PathVariable String id, @Valid @RequestBody AccountPatchRequest request) {
        Account patched = patchAccountUseCase.execute(id, accountRestMapper.toModel(request));
        return ResponseEntity.ok(accountRestMapper.toResponse(patched));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        deleteAccountUseCase.execute(id);
        return ResponseEntity.noContent().build();
    }
}
