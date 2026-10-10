package com.wmrt.dispatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ApiKeyAuthFilter")
class ApiKeyAuthFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private ObjectMapper objectMapper;
    private static final String VALID_KEY = "test-secret-key-123";

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Allows request when valid X-API-KEY header is provided")
    void doFilter_withValidKey_passesThrough() throws ServletException, IOException {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(objectMapper, true, VALID_KEY);

        when(request.getRequestURI()).thenReturn("/api/v1/zones");
        when(request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER)).thenReturn(VALID_KEY);

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Rejects request with 401 when X-API-KEY header is missing")
    void doFilter_withMissingKey_returns401() throws ServletException, IOException {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(objectMapper, true, VALID_KEY);

        when(request.getRequestURI()).thenReturn("/api/v1/reservations");
        when(request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER)).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
        assertTrue(stringWriter.toString().contains("Invalid or missing API Key"));
    }

    @Test
    @DisplayName("Rejects request with 401 when X-API-KEY is invalid")
    void doFilter_withInvalidKey_returns401() throws ServletException, IOException {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(objectMapper, true, VALID_KEY);

        when(request.getRequestURI()).thenReturn("/api/v1/reservations");
        when(request.getHeader(ApiKeyAuthFilter.API_KEY_HEADER)).thenReturn("wrong-key");

        StringWriter stringWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        filter.doFilter(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Allows request when filter is disabled")
    void doFilter_whenDisabled_passesWithoutKey() throws ServletException, IOException {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(objectMapper, false, VALID_KEY);

        when(request.getRequestURI()).thenReturn("/api/v1/zones");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Allows non-api paths without key")
    void doFilter_nonApiPath_passesWithoutKey() throws ServletException, IOException {
        ApiKeyAuthFilter filter = new ApiKeyAuthFilter(objectMapper, true, VALID_KEY);

        when(request.getRequestURI()).thenReturn("/health");

        filter.doFilter(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(anyInt());
    }
}
