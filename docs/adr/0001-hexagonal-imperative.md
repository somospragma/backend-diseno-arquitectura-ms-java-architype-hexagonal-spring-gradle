# ADR 0001: Arquitectura hexagonal imperativa (Spring MVC)

## Estado
Aceptado.

## Contexto
Necesitamos un servicio backend mantenible, testeable y de modelo de programacion simple
(thread-per-request), alineado al estandar Pragma java-spring (imperativo/blocking).

## Decision
Adoptar arquitectura hexagonal multi-modulo (domain/ports/usecases + infrastructure +
application) con Spring MVC (Tomcat) y JPA/Hibernate. El dominio no depende de framework
ni de `jakarta.persistence`; los adaptadores implementan puertos con tipos Java planos
(`T`, `Optional<T>`, `List<T>`), nunca `Mono`/`Flux`. El HTTP saliente usa `RestClient`
(nunca `RestTemplate` deprecated ni `WebClient` de WebFlux).

## Consecuencias
- (+) Dominio testeable sin contenedor; cambio de infraestructura sin tocar negocio.
- (+) Reglas verificables con ArchUnit (regla de dependencia hacia adentro, `@RestController`
  obligatorio en entry-points, prohibicion de `WebClient`).
- (+) Modelo mental simple (sin event loop); depuracion directa con stack traces completos.
- (−) El modelo thread-per-request escala peor bajo alta concurrencia I/O-bound que el
  reactivo; se compensa con pool de conexiones y timeouts bien dimensionados.
