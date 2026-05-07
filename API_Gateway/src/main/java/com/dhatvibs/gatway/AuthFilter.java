package com.dhatvibs.gatway;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class AuthFilter extends
        AbstractGatewayFilterFactory<AuthFilter.Config> {

    @Value("${jwt.secret}")
    private String secret;

    public AuthFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            // Check Authorization header exists
            if (!request.getHeaders()
                        .containsKey("Authorization")) {
                return onError(exchange,
                    "Missing Authorization header",
                    HttpStatus.UNAUTHORIZED);
            }

            String authHeader = request.getHeaders()
                                       .getFirst("Authorization");

            if (authHeader == null
                    || !authHeader.startsWith("Bearer ")) {
                return onError(exchange,
                    "Invalid Authorization format",
                    HttpStatus.UNAUTHORIZED);
            }

            // Extract token
            String token = authHeader.substring(7);

            try {
                // Validate and parse JWT
                Key key = Keys.hmacShaKeyFor(
                    secret.getBytes(StandardCharsets.UTF_8));

                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

                // Extract userId and appId from token
                String userId = claims.getSubject();
                String appId  = claims.get("appId", String.class);

                // Inject into request headers
                // Chatbot service reads these headers
                ServerHttpRequest mutatedRequest = request
                    .mutate()
                    .header("X-User-Id", userId)
                    .header("X-App-Id",  appId)
                    .build();
                
                  

                return chain.filter(
                    exchange.mutate()
                            .request(mutatedRequest)
                            .build());

            } catch (ExpiredJwtException e) {
                return onError(exchange,
                    "Token expired",
                    HttpStatus.UNAUTHORIZED);
            } catch (Exception e) {
                return onError(exchange,
                    "Invalid token",
                    HttpStatus.UNAUTHORIZED);
            }
        };
    }

    // Return error response
    private Mono<Void> onError(ServerWebExchange exchange,
                                String message,
                                HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
        org.springframework.core.io.buffer.DataBuffer buffer =
            response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // config class required by
        // AbstractGatewayFilterFactory
    }
}