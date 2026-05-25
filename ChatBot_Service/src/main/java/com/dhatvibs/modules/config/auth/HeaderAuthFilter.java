package com.dhatvibs.modules.config.auth;
  

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

@Component
public class HeaderAuthFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secret;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip filter for public endpoints
        if (path.startsWith("/auth/")
        		|| path.startsWith("/rider/") 
        		|| path.startsWith("/consumer/")
        		|| path.startsWith("/vendor/")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/webjars")) {
            filterChain.doFilter(request, response);
            return;
        }

        // ── Path 1: Headers injected by Gateway ──
        // When request comes through API Gateway
        String userId = request.getHeader("X-User-Id");
        String appId  = request.getHeader("X-App-Id");

        if (userId != null && !userId.isBlank()
                && appId != null && !appId.isBlank()) {
            // Gateway already validated JWT
            // Just set security context
            setAuthentication(userId, appId);
            filterChain.doFilter(request, response);
            return;
        }

        // ── Path 2: Direct JWT in Authorization header ──
        // When testing directly via Swagger on port 8082
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null
                && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Key key = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

                userId = claims.getSubject();
                appId  = claims.get("appId", String.class);

                setAuthentication(userId, appId);
                filterChain.doFilter(request, response);
                return;

            } catch (ExpiredJwtException e) {
                response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter()
                    .write("Token expired");
                return;
            } catch (Exception e) {
                response.setStatus(
                    HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter()
                    .write("Invalid token");
                return;
            }
        }

        // ── No auth provided ──
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.getWriter()
            .write("Missing Authorization header");
    }

    private void setAuthentication(
            String userId, String appId) {
        UsernamePasswordAuthenticationToken auth =
            new UsernamePasswordAuthenticationToken(
                userId,
                appId,
                List.of(new SimpleGrantedAuthority(
                    "ROLE_" + appId)));
        SecurityContextHolder.getContext()
            .setAuthentication(auth);
    }
}