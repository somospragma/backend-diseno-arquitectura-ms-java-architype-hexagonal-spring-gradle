package co.com.pragma.accounts.infrastructure.client;

import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

/**
 * Wiring del RestClient hacia el servicio externo de score. Fija timeouts de
 * conexion y lectura (RES-O-001): nunca esperas ilimitadas. El HTTP saliente
 * usa RestClient (Spring 6.1+), NUNCA RestTemplate (deprecated) ni WebClient.
 */
@Configuration
public class CreditScoreClientConfig {

    @Bean
    public RestClient creditScoreRestClient(
            @Value("${credit-score.base-url:https://credit-score.internal}") String baseUrl,
            @Value("${credit-score.connect-timeout-ms:2000}") long connectTimeoutMs,
            @Value("${credit-score.read-timeout-ms:3000}") long readTimeoutMs) {

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofMillis(connectTimeoutMs));
        factory.setReadTimeout(Duration.ofMillis(readTimeoutMs));

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .build();
    }
}
