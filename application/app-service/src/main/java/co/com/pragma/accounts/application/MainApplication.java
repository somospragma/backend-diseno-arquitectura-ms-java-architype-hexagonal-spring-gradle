package co.com.pragma.accounts.application;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Los modulos hexagonales viven en subpaquetes distintos al de la app, por eso
 * se declaran explicitamente el scan de componentes, el de repositorios JPA y el
 * de entidades sobre el paquete raiz co.com.pragma.accounts.
 */
@SpringBootApplication(scanBasePackages = "co.com.pragma.accounts")
@ConfigurationPropertiesScan("co.com.pragma.accounts.application.config")
@EnableJpaRepositories("co.com.pragma.accounts")
@EntityScan("co.com.pragma.accounts")
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
