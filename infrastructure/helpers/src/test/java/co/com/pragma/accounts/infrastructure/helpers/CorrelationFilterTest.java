package co.com.pragma.accounts.infrastructure.helpers;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class CorrelationFilterTest {

    private final CorrelationFilter filter = new CorrelationFilter();

    @Test
    void shouldGenerateTraceIdWhenHeaderAbsent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
            assertThat(MDC.get(CorrelationFilter.TRACE_ID)).isNotBlank();

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationFilter.HEADER)).isNotBlank();
        // El MDC se limpia despues de la peticion (no filtra a hilos reutilizados).
        assertThat(MDC.get(CorrelationFilter.TRACE_ID)).isNull();
    }

    @Test
    void shouldPropagateIncomingTraceId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(CorrelationFilter.HEADER, "trace-123");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = (req, res) ->
            assertThat(MDC.get(CorrelationFilter.TRACE_ID)).isEqualTo("trace-123");

        filter.doFilter(request, response, chain);

        assertThat(response.getHeader(CorrelationFilter.HEADER)).isEqualTo("trace-123");
    }
}
