package co.com.pragma.accounts.model.exception;

import java.util.Map;

/**
 * Error tecnico / de infraestructura (dependencia caida, timeout, I/O). Se
 * traduce a 5xx y nunca expone el detalle interno al cliente.
 */
public class TechnicalException extends DomainException {

    public TechnicalException(String code, String message, Throwable cause) {
        super(code, message, Map.of(), cause);
    }

    public TechnicalException(String code, String message, Map<String, Object> metadata, Throwable cause) {
        super(code, message, metadata, cause);
    }

    @Override
    public Kind kind() {
        return Kind.TECHNICAL;
    }
}
