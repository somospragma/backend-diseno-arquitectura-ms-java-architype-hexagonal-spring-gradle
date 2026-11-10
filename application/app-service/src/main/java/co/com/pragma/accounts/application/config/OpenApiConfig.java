package co.com.pragma.accounts.application.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Documentacion de API viva con OpenAPI/springdoc (API-O-004 / DOC-O-002).
 * La UI queda en /swagger-ui.html y la spec en /v3/api-docs.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI accountsOpenApi() {
        return new OpenAPI().info(new Info()
            .title("Accounts Service API")
            .version("v1")
            .description("Arquetipo Pragma Java Spring MVC - API de cuentas (showcase de madurez)."));
    }
}
