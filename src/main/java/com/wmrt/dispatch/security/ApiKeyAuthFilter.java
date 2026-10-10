package com.wmrt.dispatch.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wmrt.dispatch.dto.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

/**
 * Filter that enforces API Key authentication via the X-API-KEY HTTP header.
 * Intercepts all requests matching /api/**.
 */
@Component
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(ApiKeyAuthFilter.class);
    public static final String API_KEY_HEADER = "X-API-KEY";

    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private final String configuredApiKey;

    public ApiKeyAuthFilter(
            ObjectMapper objectMapper,
            @Value("${security.api-key.enabled:true}") boolean enabled,
            @Value("${security.api-key.value:wmrt-secret-api-key-2026}") String configuredApiKey) {
        this.objectMapper = objectMapper;
        this.enabled = enabled;
        this.configuredApiKey = configuredApiKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Only enforce for /api/** endpoints when enabled
        if (!enabled || !path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String requestApiKey = request.getHeader(API_KEY_HEADER);

        if (requestApiKey == null || !requestApiKey.equals(configuredApiKey)) {
            log.warn("Unauthorized access attempt to {} from IP {}: missing or invalid API Key",
                    path, request.getRemoteAddr());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");

            ErrorResponse error = new ErrorResponse(
                    Instant.now().toString(),
                    HttpStatus.UNAUTHORIZED.value(),
                    "Unauthorized",
                    "Invalid or missing API Key. Please provide a valid header: " + API_KEY_HEADER,
                    path
            );

            response.getWriter().write(objectMapper.writeValueAsString(error));
            return;
        }

        filterChain.doFilter(request, response);
    }
}
