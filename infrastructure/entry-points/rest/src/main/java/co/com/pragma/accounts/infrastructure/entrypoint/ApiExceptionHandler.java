package co.com.pragma.accounts.infrastructure.entrypoint;

import co.com.pragma.accounts.infrastructure.helpers.CorrelationFilter;
import co.com.pragma.accounts.model.exception.BusinessException;
import co.com.pragma.accounts.model.exception.ErrorCatalog;
import co.com.pragma.accounts.model.exception.TechnicalException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejo de errores centralizado en formato RFC 7807 (Problem Details) con
 * extensiones de negocio (code, metadata, traceId) via el mecanismo de extension
 * del RFC. Version imperativa con @RestControllerAdvice + ProblemDetail (Spring MVC).
 *
 * - Separa errores de NEGOCIO (4xx) de TECNICOS (5xx) por el tipo de DomainException.
 * - Nunca expone internals (stack trace / mensaje tecnico) en 5xx: mensaje generico.
 * - El code de negocio embebe el status HTTP: ACC-&lt;status&gt;-&lt;seq&gt;.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String TYPE_BASE = "https://errors.pragma.co/";

    @ExceptionHandler(BusinessException.class)
    public ProblemDetail handleBusiness(BusinessException ex, HttpServletRequest request) {
        HttpStatus status = statusForBusinessCode(ex.code());
        return problem(status, "Business rule violation", ex.getMessage(), request,
            ex.code(), ex.metadata());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, Object> meta = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(fe -> meta.put(fe.getField(), fe.getDefaultMessage()));
        return problem(HttpStatus.BAD_REQUEST, "Validation error",
            ErrorCatalog.VALIDATION_ERROR.defaultMessage(), request,
            ErrorCatalog.VALIDATION_ERROR.code(), meta);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return problem(HttpStatus.BAD_REQUEST, "Invalid request", ex.getMessage(), request,
            ErrorCatalog.VALIDATION_ERROR.code(), Map.of());
    }

    @ExceptionHandler(TechnicalException.class)
    public ProblemDetail handleTechnical(TechnicalException ex, HttpServletRequest request) {
        // Error tecnico: se loguea con detalle, pero al cliente solo mensaje generico.
        log.error("Technical error [{}] traceId={}", ex.code(), traceId(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
            ErrorCatalog.UNEXPECTED_ERROR.defaultMessage(), request, ex.code(), Map.of());
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex, HttpServletRequest request) {
        log.error("Unhandled error traceId={}", traceId(), ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error",
            ErrorCatalog.UNEXPECTED_ERROR.defaultMessage(), request,
            ErrorCatalog.UNEXPECTED_ERROR.code(), Map.of());
    }

    private HttpStatus statusForBusinessCode(String code) {
        try {
            String[] parts = code.split("-");
            return HttpStatus.valueOf(Integer.parseInt(parts[1]));
        } catch (RuntimeException e) {
            return HttpStatus.UNPROCESSABLE_ENTITY;
        }
    }

    private ProblemDetail problem(HttpStatus status, String title, String detail,
                                  HttpServletRequest request, String code, Map<String, Object> metadata) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail == null ? title : detail);
        pd.setTitle(title);
        pd.setType(URI.create(TYPE_BASE + code));
        pd.setInstance(URI.create(request.getRequestURI()));
        // Extensiones RFC 7807 (mecanismo de extension del propio RFC):
        pd.setProperty("code", code);
        String traceId = traceId();
        if (!traceId.isBlank()) {
            pd.setProperty("traceId", traceId);
        }
        if (metadata != null && !metadata.isEmpty()) {
            pd.setProperty("metadata", metadata);
        }
        return pd;
    }

    private String traceId() {
        String traceId = MDC.get(CorrelationFilter.TRACE_ID);
        return traceId == null ? "" : traceId;
    }
}
