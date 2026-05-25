package com.dhatvibs.modules.consumer.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation
        .Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class ConsumerApiClient {

    @Value("${consumer.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate =
        new RestTemplate();
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    // ── ORDERS ────────────────────────────────────

    public JsonNode getOrdersByUser(
            String userId, String token) {
        return get("/api/orders/user/"
                   + userId, token);
    }

    public JsonNode getOrderById(
            String orderId, String token) {
        return get("/api/orders/"
                   + orderId, token);
    }

    public JsonNode cancelOrder(
            String orderId, String token) {
        try {
            HttpEntity<String> entity =
                new HttpEntity<>(jsonHeaders(token));
            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + "/api/orders/"
                    + orderId + "/cancel",
                    HttpMethod.POST,
                    entity, String.class);
            if (res.getStatusCode()
                    .is2xxSuccessful()
                    && res.getBody() != null) {
                return objectMapper.readTree(
                    res.getBody());
            }
        } catch (Exception e) {
            log.error("cancelOrder failed: {}",
                      e.getMessage());
        }
        return null;
    }

    // ── PROFILE ───────────────────────────────────

    public JsonNode getAddresses(String token) {
        return get("/api/users/addresses", token);
    }

    // ── RATINGS ───────────────────────────────────

    public JsonNode getUserRatings(String token) {
        return get(
            "/api/orders/ratings/user", token);
    }

    // ── GENERIC ───────────────────────────────────

    private JsonNode get(
            String path, String token) {
        try {
            HttpEntity<String> entity =
                new HttpEntity<>(
                    jsonHeaders(token));
            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.GET,
                    entity, String.class);
            log.info("GET {} → {}",
                     path, res.getStatusCode());
            if (res.getStatusCode()
                    .is2xxSuccessful()
                    && res.getBody() != null) {
                return objectMapper.readTree(
                    res.getBody());
            }
        } catch (Exception e) {
            log.error("GET {} failed: {}",
                      path, e.getMessage());
        }
        return null;
    }

    private HttpHeaders jsonHeaders(String token) {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(
            MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank())
            h.setBearerAuth(token);
        return h;
    }
}