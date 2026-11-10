# accounts-service (Artefacto showcase — Java Spring MVC / Gradle)

Microservicio imperativo (blocking) de referencia de Pragma que cumple **punto a punto** el Framework de Madurez Backend (Obligatorios + Best Practice). Un desarrollador puede clonarlo, correrlo y usarlo como base o como muestra del "cómo se hace" cada práctica en el stack `java-spring`.

## Información General

- **Framework**: Spring Boot 4.x (Spring MVC, thread-per-request, Tomcat)
- **Lenguaje**: Java 21
- **Build**: Gradle multi-módulo (Kotlin DSL) con version catalog
- **Arquitectura**: Hexagonal (puertos y adaptadores)
- **Persistencia**: JPA / Hibernate (PostgreSQL; H2 en tests)
- **HTTP saliente**: `RestClient` (nunca `RestTemplate` ni `WebClient`)
- **Null-safety**: JSpecify (`@NullMarked` por paquete de dominio)
- **Paquete base**: `co.com.pragma.accounts`
- **Cobertura**: gate de 90% de línea (JaCoCo) enganchado a `check`

## Comandos

```bash
./gradlew test                              # tests unitarios + integración
./gradlew check                             # tests + gate de cobertura 90% + arch tests
./gradlew jacocoRootReport                  # reporte de cobertura unificado (build/reports/jacoco)
./gradlew jacocoRootCoverageVerification    # gate de cobertura (falla si < 90%)
./gradlew :domain:usecases:pitest           # mutation testing del dominio
./gradlew :application:app-service:bootRun  # ejecutar el servicio
```

## Estructura

```
accounts-service/
├── domain/
│   ├── model/         # Account, CreditScore, excepciones (RFC7807 code/metadata), @Sensitive
│   ├── ports/         # IAccountGateway, ICreditScoreGateway (tipos planos Optional/List)
│   └── usecases/      # Create/Get/Delete/GetCreditScore UseCase (clases planas, sin Spring)
├── infrastructure/
│   ├── entry-points/rest/             # @RestController + DTOs record + MapStruct
│   │                                  #   + @RestControllerAdvice RFC7807 + security headers + validación
│   ├── driven-adapters/
│   │   ├── persistence/               # JPA/Hibernate, paginación con tope (Pageable)
│   │   └── external-service-client-api/  # RestClient resiliente (timeout+retry+CB+bulkhead)
│   └── helpers/       # CorrelationFilter (MDC), DataMasker, AesGcmCipher
└── application/app-service/  # MainApplication, UseCasesConfig, config, application.yml, logback-spring.xml
```

## API

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/v1/accounts` | Crea una cuenta (201) |
| `GET` | `/api/v1/accounts/{id}` | Obtiene una cuenta (404 si no existe) |
| `GET` | `/api/v1/accounts?page=&size=` | Lista paginada (tope duro de 100) |
| `GET` | `/api/v1/accounts/{id}/credit-score` | Score crediticio (servicio externo resiliente) |
| `DELETE` | `/api/v1/accounts/{id}` | Elimina una cuenta (204) |

## Variables de Entorno

| Variable | Descripción | Default |
|----------|-------------|---------|
| `SERVICE_NAME` | Nombre del servicio | `accounts-service` |
| `SERVER_PORT` | Puerto HTTP | `8080` |
| `DB_URL` / `DB_USER` / `DB_PASSWORD` | JDBC PostgreSQL (JPA) | — |
| `CREDIT_SCORE_URL` | Servicio externo de score | `https://credit-score.internal` |

## Prácticas del Framework de Madurez cubiertas

- **Errores**: RFC 7807 (`ProblemDetail`) + extensiones `code`/`metadata`/`traceId`; negocio vs técnico; nunca expone internals.
- **API**: métodos y códigos HTTP correctos, paginación con tope, OpenAPI (springdoc), headers de seguridad.
- **Seguridad**: validación de entrada (Bean Validation), headers de seguridad, no-leak de errores en 5xx.
- **Datos sensibles**: `@Sensitive`, masking en logs y respuestas, AES-256-GCM.
- **Resiliencia**: timeout (RestClient) + retry (backoff+jitter) + circuit breaker + bulkhead (resilience4j) en el cliente externo.
- **Observabilidad**: logs JSON, correlation id (MDC), health liveness/readiness, métricas Prometheus.
- **Config**: externalizada, perfiles (dev/prod), validada al arranque (fail fast).
- **Calidad**: pirámide de tests (unit + slice + integración), ArchUnit, cobertura 90% gate, PIT en dominio.

Ver `docs/` para catálogo de errores, ADRs, threat model, glosario y runbook.

## Notas del Artefacto

- Estándar imperativo: usa `@RestController`, JPA, `RestClient` y Tomcat. **Nunca** `WebClient` (es de WebFlux) ni `RestTemplate` (deprecated). ArchUnit lo verifica.
- Los use cases son clases Java planas (sin `@Service`/`@Component`); se auto-registran vía `UseCasesConfig` por convención de nombre (`^.+UseCase$`).
- Los tests corren sin Docker (H2 en memoria). Testcontainers queda configurado para su ejecución en CI con infraestructura real.
