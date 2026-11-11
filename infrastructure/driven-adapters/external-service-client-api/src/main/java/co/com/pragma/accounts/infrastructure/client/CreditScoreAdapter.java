package co.com.pragma.accounts.infrastructure.client;

import co.com.pragma.accounts.model.CreditScore;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import co.com.pragma.accounts.model.exception.TechnicalException;
import co.com.pragma.accounts.ports.ICreditScoreGateway;
import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Adapter resiliente hacia el servicio externo de score (RES-O-001/002/005),
 * version imperativa. Decora la llamada sincrona del RestClient, de adentro hacia
 * afuera: llamada -&gt; Bulkhead -&gt; CircuitBreaker -&gt; Retry. El timeout lo impone
 * el RequestFactory del RestClient (connect/read). Nunca propaga el error tecnico
 * crudo: lo mapea a TechnicalException para un tratamiento uniforme.
 */
@Component
@RequiredArgsConstructor
public class CreditScoreAdapter implements ICreditScoreGateway {

    private static final String INSTANCE = "creditScore";

    private final RestClient creditScoreRestClient;
    private final CreditScoreMapper mapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RetryRegistry retryRegistry;
    private final BulkheadRegistry bulkheadRegistry;

    @Override
    public CreditScore getScore(String accountId) {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker(INSTANCE);
        Retry retry = retryRegistry.retry(INSTANCE);
        Bulkhead bulkhead = bulkheadRegistry.bulkhead(INSTANCE);

        Supplier<CreditScore> call = () -> {
            CreditScoreDto dto = creditScoreRestClient.get()
                .uri("/api/v1/scores/{id}", accountId)
                .retrieve()
                .body(CreditScoreDto.class);
            if (dto == null) {
                throw new TechnicalException(ErrorCatalog.UNEXPECTED_ERROR.code(),
                    "Empty response from credit score provider", null);
            }
            return mapper.toModel(dto);
        };

        // Composicion de decoradores (de adentro hacia afuera): la llamada se
        // envuelve en Bulkhead, luego CircuitBreaker, y por fuera el Retry.
        Supplier<CreditScore> withBulkhead = Bulkhead.decorateSupplier(bulkhead, call);
        Supplier<CreditScore> withCb = CircuitBreaker.decorateSupplier(cb, withBulkhead);
        Supplier<CreditScore> decorated = Retry.decorateSupplier(retry, withCb);

        try {
            return decorated.get();
        } catch (TechnicalException te) {
            throw te;
        } catch (RuntimeException e) {
            throw new TechnicalException(ErrorCatalog.UNEXPECTED_ERROR.code(),
                "Credit score provider call failed", e);
        }
    }
}
