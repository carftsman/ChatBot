package com.dhatvibs.modules.config.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@Component
public class AdminApiKeyFilter extends OncePerRequestFilter {

    private static final String HEADER = "X-Admin-Api-Key";

    @Value("${chatbot.admin.api-key:}")
    private String configuredApiKey;

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        return !isAdminPath(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        if (configuredApiKey == null
                || configuredApiKey.isBlank()) {
            response.setStatus(
                HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Admin API key not configured\"}");
            return;
        }

        String provided = request.getHeader(HEADER);
        boolean ok = configuredApiKey.equals(provided);

        if (!ok) {
            response.setStatus(
                HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write(
                "{\"error\":\"Invalid or missing "
                + HEADER + "\"}");
            return;
        }

        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                "admin",
                null,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_ADMIN")));
        SecurityContextHolder.getContext()
            .setAuthentication(auth);
        chain.doFilter(request, response);
    }

    private static boolean isAdminPath(String path) {
        return path.contains("/admin/");
    }
}
