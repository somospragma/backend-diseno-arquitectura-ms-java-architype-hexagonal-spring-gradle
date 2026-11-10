package co.com.pragma.accounts.infrastructure.persistence;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;

/**
 * Config minima para las pruebas de slice de persistencia (@DataJpaTest). El
 * modulo no contiene la MainApplication (vive en app-service), por eso se declara
 * un @SpringBootConfiguration local que la slice detecta al buscar hacia arriba.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
class TestBootConfig {
}
