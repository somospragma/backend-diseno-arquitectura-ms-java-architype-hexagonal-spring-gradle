# Threat Model (resumen)

Modelado de amenazas del servicio de cuentas (SEG-BP-012). Se listan los casos de abuso considerados en diseño y el control que los mitiga.

## Activos
- Datos de cuenta (incluye PII: email).
- Disponibilidad del servicio.

## Amenazas y controles (STRIDE resumido)

| Amenaza | Escenario de abuso | Control en el artefacto |
|---------|--------------------|--------------------------|
| Spoofing | Peticiones sin autenticar a operaciones sensibles | Deny-by-default + auth (Spring Security), token validado server-side |
| Tampering | Inyección SQL vía parámetros | Queries parametrizadas (JPA/Hibernate), validación de entrada |
| Repudiation | Falta de trazabilidad | Correlation id + logs estructurados por petición |
| Information disclosure | Fuga de PII en logs/respuestas; stack traces en errores | Masking en logs y respuestas; RFC 7807 sin internals en 5xx |
| Denial of Service | Colección ilimitada / payloads gigantes | Paginación con tope, límites de payload, rate limiting |
| Elevation of privilege | Acceso a recurso ajeno por ID (IDOR) | Autorización a nivel de objeto (verificar propiedad por ID) |
| SSRF | Forzar llamadas a destinos internos | Allowlist de destinos en llamadas salientes con input del usuario |
| Cascada de fallos | Dependencia externa caída | Timeout + circuit breaker + bulkhead + degradación |
| Envenenamiento de cola | Mensaje que rompe el consumidor | Aislamiento por mensaje + DLQ (el poison no detiene el consumo) |

## Notas
Este documento es una muestra; en un servicio real se profundiza por flujo y se revisa periódicamente.
