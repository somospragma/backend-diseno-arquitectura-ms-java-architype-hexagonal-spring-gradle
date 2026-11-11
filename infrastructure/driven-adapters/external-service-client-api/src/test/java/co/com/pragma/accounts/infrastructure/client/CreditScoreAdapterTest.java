package co.com.pragma.accounts.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import co.com.pragma.accounts.model.CreditScore;
import co.com.pragma.accounts.model.exception.TechnicalException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

/**
 * Test del adapter resiliente con MockRestServiceServer enlazado al RestClient.
 * Verifica el happy path (mapeo DTO-&gt;dominio) y la traduccion de fallos de
 * infraestructura a TechnicalException. Retry con 1 solo intento para no alargar.
 */
class CreditScoreAdapterTest {

    private MockRestServiceServer server;
    private CreditScoreAdapter adapter;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("http://credit-score.test");
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();

        // Retry con 1 intento: aisla el test de la logica de reintentos/backoff.
        RetryRegistry retryRegistry = RetryRegistry.of(RetryConfig.custom().maxAttempts(1).build());
        // Fuerza el registro de la instancia usada por el adapter.
        Retry ignored = retryRegistry.retry("creditScore");

        adapter = new CreditScoreAdapter(
            restClient,
            new CreditScoreMapperImpl(),
            CircuitBreakerRegistry.ofDefaults(),
            retryRegistry,
            BulkheadRegistry.ofDefaults());
    }

    @Test
    void shouldMapSuccessfulResponse() {
        server.expect(requestTo("http://credit-score.test/api/v1/scores/acc-1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(
                "{\"accountId\":\"acc-1\",\"score\":730,\"band\":\"GOOD\"}",
                MediaType.APPLICATION_JSON));

        CreditScore result = adapter.getScore("acc-1");

        assertThat(result.score()).isEqualTo(730);
        assertThat(result.band()).isEqualTo("GOOD");
        server.verify();
    }

    @Test
    void shouldMapInfrastructureErrorToTechnicalException() {
        server.expect(requestTo("http://credit-score.test/api/v1/scores/acc-2"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> adapter.getScore("acc-2"))
            .isInstanceOf(TechnicalException.class);
    }
}
