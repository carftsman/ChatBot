package com.dhatvibs.modules.rider.filter;

import com.dhatvibs.modules.config.auth.JwtTokenValidator;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class RiderAuthFilter
        extends OncePerRequestFilter {

    private final JwtTokenValidator jwtTokenValidator;

    @Value("${jwt.rider.access.secret:}")
    private String riderAccessSecret;

    @Value("${jwt.rider.secret:}")
    private String riderSecret;

    @Value("${jwt.rider.refresh.secret:}")
    private String riderRefreshSecret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        String authHeader =
            request.getHeader("Authorization");

        if (authHeader == null
                || !authHeader.startsWith("Bearer ")) {
            sendError(response,
                HttpServletResponse.SC_FORBIDDEN,
                "Missing Authorization header");
            return;
        }

        String token = authHeader.substring(7);
        String riderId = resolveRiderId(token);

        if (riderId == null) {
            sendError(response,
                HttpServletResponse.SC_UNAUTHORIZED,
                "Invalid or expired token");
            return;
        }

        log.info("Rider authenticated: {}", riderId);
        setAuth(riderId);
        chain.doFilter(request, response);
    }

    private String resolveRiderId(String token) {
        Claims claims = jwtTokenValidator.verifyAndGetClaims(
            token,
            List.of(
                riderAccessSecret,
                riderSecret,
                riderRefreshSecret));
        if (claims == null) {
            return null;
        }
        String type = claims.get("type", String.class);
        if (type != null && !"access".equals(type)) {
            log.warn("Wrong token type: {}", type);
            return null;
        }
        return jwtTokenValidator.extractFirstPresent(
            claims, "riderId", "rider_id", "sub", "id");
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/rider/auth/")
            || path.startsWith("/ws/rider/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")
            || path.startsWith("/webjars");
    }

    private void setAuth(String riderId) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                riderId,
                null,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_RIDER")));
        SecurityContextHolder.getContext()
            .setAuthentication(auth);
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        response.getWriter()
            .write("{\"error\":\"" + message + "\"}");
    }

    @Override
    protected boolean shouldNotFilter(
            HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/consumer/")
            || path.startsWith("/vendor/")
            || path.startsWith("/riderchatbot/")
            || path.contains("/admin/");
    }
}
