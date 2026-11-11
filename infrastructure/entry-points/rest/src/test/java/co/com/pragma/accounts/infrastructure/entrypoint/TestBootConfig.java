package co.com.pragma.accounts.infrastructure.entrypoint;

import org.springframework.boot.SpringBootConfiguration;

/**
 * Config minima para las pruebas de slice de este modulo (@WebMvcTest). El
 * modulo de entry-points no contiene la MainApplication (vive en app-service),
 * por eso se declara un @SpringBootConfiguration local que la slice detecta al
 * buscar hacia arriba. La autoconfiguracion web la aporta la propia slice.
 */
@SpringBootConfiguration
class TestBootConfig {
}
