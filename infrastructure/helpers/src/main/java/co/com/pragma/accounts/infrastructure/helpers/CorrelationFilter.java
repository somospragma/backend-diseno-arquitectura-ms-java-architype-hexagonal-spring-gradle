package co.com.pragma.accounts.infrastructure.helpers;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Correlacion de peticiones (OBS-O-002) para el stack imperativo (thread-per-request).
 * Toma o genera el traceId, lo devuelve en el header de respuesta y lo publica en
 * MDC para que TODOS los logs de la peticion lo lleven. Limpia el MDC en finally
 * para no filtrar el valor a hilos reutilizados por el pool de Tomcat.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationFilter extends OncePerRequestFilter {

    public static final String TRACE_ID = "traceId";
    public static final String HEADER = "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String incoming = request.getHeader(HEADER);
        String traceId = (incoming == null || incoming.isBlank())
            ? UUID.randomUUID().toString()
            : incoming;

        response.addHeader(HEADER, traceId);
        MDC.put(TRACE_ID, traceId);
        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID);
        }
    }
}
