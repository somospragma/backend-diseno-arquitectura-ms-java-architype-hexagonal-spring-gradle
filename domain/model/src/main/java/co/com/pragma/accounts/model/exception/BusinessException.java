package co.com.pragma.accounts.model.exception;

import java.util.Map;

/**
 * Error de negocio (regla de dominio, validacion semantica, recurso no
 * encontrado, conflicto). Se traduce a 4xx. El cliente puede corregir.
 */
public class BusinessException extends DomainException {

    public BusinessException(String code, String message) {
        this(code, message, Map.of());
    }

    public BusinessException(String code, String message, Map<String, Object> metadata) {
        super(code, message, metadata, null);
    }

    @Override
    public Kind kind() {
        return Kind.BUSINESS;
    }
}
