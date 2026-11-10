# Runbook Operativo

Guía operativa del servicio (DOC-BP-005). Muestra de referencia.

## Despliegue
1. Construir: `./gradlew :application:app-service:bootJar`
2. Imagen: `docker build -t accounts-service:<tag> .` (multi-stage, no-root).
3. Variables requeridas: `DB_URL`, `DB_USER`, `DB_PASSWORD` (ver README).
4. Verificar readiness antes de recibir tráfico: `GET /actuator/health/readiness`.

## Health y observabilidad
- Liveness: `GET /actuator/health/liveness`
- Readiness (incluye base de datos JPA): `GET /actuator/health/readiness`
- Métricas Prometheus: `GET /actuator/prometheus`
- Logs: JSON estructurado a stdout, con `traceId` por petición.

## Rollback
- Redesplegar la versión anterior (imagen con tag pineado). El servicio es stateless.

## Incidentes comunes
| Síntoma | Diagnóstico | Acción |
|---------|-------------|--------|
| Readiness DOWN | Dependencia (BD) caída | Revisar `health/readiness`, conectividad JDBC/pool Hikari |
| Latencia alta hacia score | Dependencia externa lenta | Revisar estado del circuit breaker (`creditScore`) en métricas |
| Pool de conexiones agotado | Fugas de conexión / carga alta | Revisar métricas Hikari (`hikaricp.connections.*`), timeouts y tamaño de pool |
| OOM | Colección grande en memoria | Verificar paginación con tope (page/size); revisar límites de heap del contenedor |
