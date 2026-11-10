# Glosario de Dominio (Lenguaje Ubicuo)

Vocabulario compartido entre negocio y código (NOM-BP-008). Los nombres del código reflejan este glosario.

| Término | Definición |
|---------|------------|
| Account | Cuenta de un titular. Tiene tipo, saldo, estado y datos de contacto (email es PII). |
| Account Type | Clasificación de la cuenta (SAVINGS, CHECKING). |
| Account Status | Estado del ciclo de vida (ACTIVE, ...). |
| Credit Score | Puntaje crediticio consultado a un proveedor externo. |
| Account Event | Hecho ocurrido sobre una cuenta (creada, actualizada), almacenado en el historial. |
| Gateway | Puerto de salida del dominio hacia un sistema externo (nombre de dominio, nunca del sistema). |
| Use Case | Operación de negocio orquestada por la capa de aplicación, libre de framework. |
