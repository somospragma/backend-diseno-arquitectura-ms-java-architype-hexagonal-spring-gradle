# Catálogo de Códigos de Error

Códigos de negocio estables y versionados (ERR-BP-008). Los consumidores reaccionan por `code`, no por el texto. El código embebe el status HTTP: `ACC-<status>-<seq>`.

| Código | HTTP | Título | Cuándo ocurre |
|--------|------|--------|---------------|
| `ACC-404-001` | 404 | Resource not found | La cuenta solicitada no existe |
| `ACC-409-001` | 409 | Conflict | Ya existe una cuenta con ese identificador |
| `ACC-422-001` | 422 | Unprocessable | La cuenta está en un estado inválido para la operación |
| `ACC-400-001` | 400 | Validation error | La petición no pasó la validación de entrada |
| `ACC-503-001` | 503 | Service unavailable | El proveedor de notificaciones/score no está disponible |
| `ACC-500-001` | 500 | Internal server error | Error técnico inesperado (no expone detalle interno) |

## Formato de respuesta (RFC 7807 + extensiones)

```json
{
  "type": "https://errors.pragma.co/ACC-404-001",
  "title": "Business rule violation",
  "status": 404,
  "detail": "Account not found: acc-123",
  "instance": "/api/v1/accounts/acc-123",
  "code": "ACC-404-001",
  "traceId": "b1e2...",
  "metadata": { "accountId": "acc-123" }
}
```

- **Errores de negocio** → 4xx, con `code` y `metadata` de contexto.
- **Errores técnicos** → 5xx, mensaje genérico `Unexpected error`, sin stack trace ni detalles internos.
