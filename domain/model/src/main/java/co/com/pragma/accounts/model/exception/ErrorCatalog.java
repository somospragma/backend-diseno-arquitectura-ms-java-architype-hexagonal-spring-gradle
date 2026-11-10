package co.com.pragma.accounts.model.exception;

/**
 * Catalogo versionado de codigos de negocio (ERR-BP-008). Los consumidores
 * reaccionan por code, no por el texto del mensaje. Estable entre versiones.
 */
public enum ErrorCatalog {

    ACCOUNT_NOT_FOUND("ACC-404-001", "Account not found"),
    ACCOUNT_ALREADY_EXISTS("ACC-409-001", "Account already exists"),
    ACCOUNT_INVALID_STATE("ACC-422-001", "Account is in an invalid state for this operation"),
    VALIDATION_ERROR("ACC-400-001", "Request validation failed"),
    NOTIFICATION_UNAVAILABLE("ACC-503-001", "Notification provider unavailable"),
    UNEXPECTED_ERROR("ACC-500-001", "Unexpected error");

    private final String code;
    private final String defaultMessage;

    ErrorCatalog(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String code() {
        return code;
    }

    public String defaultMessage() {
        return defaultMessage;
    }
}
