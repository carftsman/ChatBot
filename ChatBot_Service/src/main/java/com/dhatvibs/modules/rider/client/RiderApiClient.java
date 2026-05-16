package com.dhatvibs.modules.rider.client;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Component
public class RiderApiClient {

    @Value("${rider.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate =
        new RestTemplate();
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    // ── AUTH ──────────────────────────────────────

	/*
	 * public Map sendOtp(String phone) { try { HttpEntity<Map> entity = new
	 * HttpEntity<>( Map.of("phone", phone), jsonHeaders(null));
	 * ResponseEntity<String> res = restTemplate.exchange( baseUrl +
	 * "/api/auth/send-otp", HttpMethod.POST, entity, String.class); return
	 * objectMapper.readValue( res.getBody(), Map.class); } catch (Exception e) {
	 * log.error("sendOtp error: {}", e.getMessage()); return Map.of("message",
	 * "OTP sent successfully"); } }
	 */
	/*
	 * public Map verifyOtp( String phone, String otp) { try { HttpEntity<Map>
	 * entity = new HttpEntity<>( Map.of("phone", phone, "otp", otp),
	 * jsonHeaders(null)); ResponseEntity<String> res = restTemplate.exchange(
	 * baseUrl + "/api/auth/verify-otp", HttpMethod.POST, entity, String.class);
	 * return objectMapper.readValue( res.getBody(), Map.class); } catch (Exception
	 * e) { log.error("verifyOtp error: {}", e.getMessage()); return null; } }
	 */ 
    
	/*
	 * public Map verifyOtp(String phone, String otp) { try { Map<String, String>
	 * body = new java.util.HashMap<>(); body.put("phone", phone); body.put("otp",
	 * otp);
	 * 
	 * HttpEntity<Map> entity = new HttpEntity<>(body, jsonHeaders(null));
	 * 
	 * log.info("Calling verifyOtp → phone: {}", phone);
	 * 
	 * ResponseEntity<String> res = restTemplate.exchange( baseUrl +
	 * "/api/auth/verify-otp", HttpMethod.POST, entity, String.class);
	 * 
	 * log.info("verifyOtp status: {}", res.getStatusCode());
	 * log.info("verifyOtp body: {}", res.getBody());
	 * 
	 * return objectMapper.readValue( res.getBody(), Map.class);
	 * 
	 * } catch (Exception e) { log.error("verifyOtp failed: {} | body: {}",
	 * e.getMessage(), e); return null; } }
	 */
    
    public Map sendOtp(String phone) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("phone", phone);

            HttpEntity<Map> entity =
                new HttpEntity<>(body, jsonHeaders(null));

            log.info("Calling sendOtp → phone: {}", phone);

            // Use static OTP endpoint for dev mode
            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + "/api/mobile/send-static-otp",
                    HttpMethod.POST,
                    entity,
                    String.class);

            log.info("sendOtp status: {}", res.getStatusCode());
            log.info("sendOtp body: {}", res.getBody());

            return objectMapper.readValue(
                res.getBody(), Map.class);

        } catch (Exception e) {
            log.error("sendOtp failed: {}", e.getMessage());
            return Map.of("message", "OTP sent successfully");
        }
    }

    public Map verifyOtp(String phone, String otp) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("phone", phone);
            body.put("otp", otp);

            HttpEntity<Map> entity =
                new HttpEntity<>(body, jsonHeaders(null));

            log.info("Calling verifyOtp → phone: {}", phone);

            // Use static OTP endpoint for dev mode
            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + "/api/mobile/verify-static-otp",
                    HttpMethod.POST,
                    entity,
                    String.class);

            log.info("verifyOtp status: {}", res.getStatusCode());
            log.info("verifyOtp body: {}", res.getBody());

            return objectMapper.readValue(
                res.getBody(), Map.class);

        } catch (Exception e) {
            log.error("verifyOtp failed: {}", e.getMessage());
            return null;
        }
    }

    // ── PROFILE ───────────────────────────────────

    public JsonNode getRiderProfile(String token) {
        return get("/api/profile/rider/profile",
                   token);
    }

    public JsonNode getWallet(String token) {
        return get("/api/profile/wallet", token);
    }

    public JsonNode getDocuments(String token) {
        return get("/api/profile/documents", token);
    }

    // ── ORDERS ────────────────────────────────────

    public JsonNode getOrderStats(String token) {
        return get("/api/orders/stats", token);
    }

	/*
	 * public JsonNode getOrderHistory(String token) { return
	 * get("/api/profile/orders/history", token); }
	 * 
	 * public JsonNode getDeliveredOrders( String token) { return
	 * get("/api/orders/delivered", token); }
	 */
    
 // Change getOrderHistory to use delivered orders endpoint
	/*
	 * public JsonNode getDeliveredOrders(String token) { return
	 * get("/api/orders/delivered", token); }
	 */
    
	/*
	 * public JsonNode getDeliveredOrders(String token) { try { HttpEntity<String>
	 * entity = new HttpEntity<>(jsonHeaders(token));
	 * 
	 * log.info("Calling delivered orders API...");
	 * log.info("URL: {}/api/orders/delivered", baseUrl);
	 * log.info("Token (first 20 chars): {}", token.substring(0,
	 * Math.min(token.length(), 20)));
	 * 
	 * ResponseEntity<String> res = restTemplate.exchange( baseUrl +
	 * "/api/orders/delivered", HttpMethod.GET, entity, String.class);
	 * 
	 * log.info("Status: {}", res.getStatusCode()); log.info("Body: {}",
	 * res.getBody() != null ? res.getBody().substring(0,
	 * Math.min(res.getBody().length(), 500)) : "NULL BODY");
	 * 
	 * if (res.getStatusCode().is2xxSuccessful() && res.getBody() != null) { return
	 * objectMapper.readTree( res.getBody()); }
	 * 
	 * log.error("Non-2xx status: {}", res.getStatusCode()); return null;
	 * 
	 * } catch (Exception e) { log.error("getDeliveredOrders FAILED: {}",
	 * e.getMessage()); log.error("Full error: ", e); return null; } }
	 */
    
    public JsonNode getDeliveredOrders(String token) {
        // /api/orders/delivered returns 404 on Node.js
        // Use /api/profile/orders/history instead
        // which is confirmed working
        return get(
            "/api/profile/orders/history?filter=all",
            token);
    }

    // Keep this but fix the path
    public JsonNode getOrderHistory(String token) {
        return get("/api/orders/delivered", token);
    }

    public JsonNode getCancelledOrders(
            String token) {
        return get("/api/orders/cancelled", token);
    }

    // ── EARNINGS ──────────────────────────────────

    public JsonNode getEarningsSummary(
            String token) {
        return get(
            "/api/rider/earnings/new/new_summary",
            token);
    }

    public JsonNode getDailyEarnings(
            String token) {
        return get(
            "/api/rider/earnings/new/new_daily",
            token);
    }

    public JsonNode getWeeklyEarnings(
            String token) {
        return get(
            "/api/rider/earnings/new/new_weekly",
            token);
    }

    public JsonNode getEarningsHistory(
            String token) {
        return get(
            "/api/rider/earnings/new/history",
            token);
    }

    public JsonNode getCashBalance(String token) {
        return get("/api/rider/cashbalance", token);
    }

    // ── RATINGS ───────────────────────────────────

    public JsonNode getRatings(String token) {
        return get("/api/rider/ratings", token);
    }

    public JsonNode getWeeklyPerformance(
            String token) {
        return get("/api/rider/rating/weekly",
                   token);
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
                    HttpMethod.GET, entity,
                    String.class);
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
        h.setContentType(MediaType.APPLICATION_JSON);
        if (token != null && !token.isBlank()) {
            h.setBearerAuth(token);
        }
        return h;
    }
}