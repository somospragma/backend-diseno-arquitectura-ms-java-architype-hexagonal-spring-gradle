package co.com.pragma.accounts.application;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

/**
 * Cuerpo vacío: los use cases (clases Java planas, sin anotaciones Spring) se
 * auto-registran como beans por convención de nombre (sufijo UseCase) mediante
 * este scan por regex. Mismo patrón que el stack imperativo de Pragma.
 */
@Configuration
@ComponentScan(
    basePackages = "co.com.pragma.accounts.usecase",
    includeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
    },
    useDefaultFilters = false
)
public class UseCasesConfig {
}
