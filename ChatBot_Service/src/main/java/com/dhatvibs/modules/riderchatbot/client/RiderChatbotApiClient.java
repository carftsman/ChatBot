package com.dhatvibs.modules.riderchatbot.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation
        .Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class RiderChatbotApiClient {

    @Value("${riderchatbot.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate =
        new RestTemplate();
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    @PostConstruct
    public void init() {
        log.info("RiderChatbotApiClient "
               + "baseUrl = {}", baseUrl);
    }

    // ── EARNINGS ──────────────────────────────────

    public JsonNode getEarningsSummary(
            String token) {
        return get(
            "/api/rider/earnings/new/new_summary",
            token);
    }

    public JsonNode getDailyEarnings(String token) {
        return get(
            "/api/rider/earnings/new/new_daily",
            token);
    }

    public JsonNode getWeeklyEarnings(String token) {
        return get(
            "/api/rider/earnings/new/new_weekly",
            token);
    }

    public JsonNode getCashBalance(String token) {
        return get("/api/rider/cashbalance", token);
    }

    // ── ORDERS ────────────────────────────────────

    public JsonNode getOrderHistory(String token) {
        return get(
            //"/api/profile/orders/history?filter=all",
        		"/api/orders/delivered",
            token);
    }

    public JsonNode getOrderStats(String token) {
        return get("/api/orders/stats", token);
    }

    public JsonNode getOrderDetails(
            String orderId, String token) {
        return get(
            "/api/orders/" + orderId + "/details",
            token);
    }

    public JsonNode getDeliveredOrders(
            String token) {
        return get("/api/orders/delivered", token);
    }

    // ── RATINGS ───────────────────────────────────

    public JsonNode getRatings(String token) {
        return get("/api/rider/ratings", token);
    }

    public JsonNode getWeeklyPerformance(
            String token) {
        return get(
            "/api/rider/rating/weekly", token);
    }

    // ── SLOTS ─────────────────────────────────────

    public JsonNode getActiveSlots(String token) {
        return get("/api/slots/activeSlots", token);
    }

    public JsonNode getSlotHistory(String token) {
        return get(
            "/api/profile/slots/history", token);
    }

    // ── PROFILE ───────────────────────────────────
    
 // Add this new method — profile orders history
    public JsonNode getDeliveredOrdersHistory(
            String token) {
        return get(
            "/api/profile/orders/history?filter=all",
            token);
    }

    public JsonNode getRiderProfile(String token) {
        return get(
            "/api/profile/rider/profile", token);
    }

    public JsonNode getWallet(String token) {
        return get("/api/profile/wallet", token);
    }

    public JsonNode getBankDetails(String token) {
        return get(
            "/api/bank/bank-details", token);
    }

    public JsonNode getDocuments(String token) {
        return get(
            "/api/profile/documents", token);
    }

    // ── INCENTIVES ────────────────────────────────

    public JsonNode getIncentives(String token) {
        return get(
            "/api/rider/incentives/with-progress",
            token);
    }

    // ── REFERRAL ──────────────────────────────────

    public JsonNode getReferralSummary(
            String token) {
        return get("/api/refer/summary", token);
    }

    // ── GENERIC ───────────────────────────────────

    private JsonNode get(
            String path, String token) {
        try {
            HttpEntity<String> entity =
                new HttpEntity<>(jsonHeaders(token));

            log.info("GET {}{}", baseUrl, path);

            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.GET,
                    entity, String.class);

            log.info("GET {} → {} | {}",
                     path,
                     res.getStatusCode(),
                     res.getBody() != null
                         ? res.getBody().substring(0,
                             Math.min(300,
                                 res.getBody()
                                    .length()))
                         : "NULL");

            if (res.getStatusCode().is2xxSuccessful()
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
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank())
            h.setBearerAuth(token);
        return h;
    }
}