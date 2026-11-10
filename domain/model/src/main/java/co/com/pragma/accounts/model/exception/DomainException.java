package co.com.pragma.accounts.model.exception;

import java.util.Map;

/**
 * Raiz de las excepciones de dominio. Separa errores de NEGOCIO de errores
 * TECNICOS mediante el campo kind. Cada excepcion lleva un code de negocio
 * estable (para el catalogo de errores) y metadata opcional para el contexto.
 */
public abstract class DomainException extends RuntimeException {

    public enum Kind {
        /** Error de negocio: el cliente puede corregir (mapea a 4xx). */
        BUSINESS,
        /** Error tecnico/infraestructura: no es culpa del cliente (mapea a 5xx). */
        TECHNICAL
    }

    private final String code;
    private final transient Map<String, Object> metadata;

    protected DomainException(String code, String message, Map<String, Object> metadata, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public abstract Kind kind();

    public String code() {
        return code;
    }

    public Map<String, Object> metadata() {
        return metadata;
    }
}
