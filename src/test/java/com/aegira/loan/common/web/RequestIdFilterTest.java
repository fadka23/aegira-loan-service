package com.aegira.loan.common.web;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import javax.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class RequestIdFilterTest {
    private final RequestIdFilter filter = new RequestIdFilter();

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void shouldUseXCorrelationIdWhenPresent() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "REQ-001");
        request.addHeader("X-Request-Id", "legacy-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, assertingChain("REQ-001"));

        assertEquals("REQ-001", response.getHeader("X-Correlation-Id"));
    }

    @Test
    void shouldFallbackToXRequestIdWhenCorrelationIdMissing() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Request-Id", "legacy-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, assertingChain("legacy-id"));

        assertEquals("legacy-id", response.getHeader("X-Correlation-Id"));
    }

    @Test
    void shouldGenerateCorrelationIdWhenHeadersMissing() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(new MockHttpServletRequest(), response, (request, servletResponse) ->
                assertNotNull(MDC.get("correlation_id")));

        assertNotNull(response.getHeader("X-Correlation-Id"));
    }

    @Test
    void shouldAddXCorrelationIdResponseHeader() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-Correlation-Id", "REQ-002");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, assertingChain("REQ-002"));

        assertEquals("REQ-002", response.getHeader("X-Correlation-Id"));
        assertEquals("REQ-002", response.getHeader("X-Request-Id"));
    }

    private FilterChain assertingChain(String expectedCorrelationId) {
        return (request, response) -> {
            assertEquals(expectedCorrelationId, MDC.get("correlation_id"));
            assertEquals(expectedCorrelationId, MDC.get("requestId"));
        };
    }
}
