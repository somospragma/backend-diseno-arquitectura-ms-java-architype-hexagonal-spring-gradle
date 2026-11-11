# ADR 0002: Modelo de error RFC 7807 con extensiones

## Estado
Aceptado.

## Contexto
Se requiere un formato de error uniforme, interoperable y con semántica de negocio para toda la API.

## Decisión
Usar RFC 7807 (Problem Details, `application/problem+json`) como estándar, extendido con propiedades de negocio (`code`, `metadata`, `traceId`) mediante el mecanismo de extensión que el propio RFC contempla. Los errores de negocio mapean a 4xx (status embebido en el `code`), los técnicos a 5xx sin exponer internals.

## Consecuencias
- (+) Interoperable con gateways/clientes; separa negocio de transporte.
- (+) Los consumidores reaccionan por `code` estable (catálogo versionado).
- (−) Requiere disciplina para mantener el catálogo de códigos sincronizado.
