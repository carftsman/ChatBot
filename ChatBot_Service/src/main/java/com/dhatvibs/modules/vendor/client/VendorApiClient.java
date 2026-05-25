package com.dhatvibs.modules.vendor.client;

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
public class VendorApiClient {

    @Value("${vendor.api.base-url}")
    private String baseUrl;

    private final RestTemplate restTemplate =
        new RestTemplate();
    private final ObjectMapper objectMapper =
        new ObjectMapper();
    
    

    @PostConstruct
    public void init() {
        log.info("=== VendorApiClient baseUrl: {} ===",
                 baseUrl);
    }

    // ── ORDERS ────────────────────────────────────

    public JsonNode getPendingOrders(String token) {
		/*
		 * return get("/api/merchant/orders/pending", token);
		 */
    	
    	// Try the correct merchant order path
        JsonNode result = get(
            "/api/merchant/orders/pending", token);

        if (result == null) {
            // Try alternative path
            result = get(
                "/api/merchant/dashboard/store/orders-list",
                token);
        }
        return result;
        
     
    }

    public JsonNode getDeliveredOrders(String token) {
        return get(
            "/api/merchant/orders/delivered", token);
    }

    public JsonNode getCancelledOrders(String token) {
        return get(
            "/api/merchant/orders/cancelled", token);
    }

    public JsonNode getOrderById(
            String orderId, String token) {
        return get(
            "/api/merchant/dashboard/store/orders/"
            + orderId, token);
    }

    // ── STORE ─────────────────────────────────────

    public JsonNode getStoreDetails(String token) {
        return get(
            "/api/merchant/dashboard/store", token);
    }

    // ── WALLET / PAYMENT ──────────────────────────

    public JsonNode getWalletBalance(String token) {
        return get(
            "/api/merchant/entitlement", token);
    }

    public JsonNode getTransactionHistory(
            String token) {
        return get(
            "/api/merchant/entitlement/transactions",
            token);
    }

    // ── GENERIC ───────────────────────────────────

    private JsonNode get(
            String path, String token) {
        try {
            HttpEntity<String> entity =
                new HttpEntity<>(jsonHeaders(token));
            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.GET, entity,
                    String.class);

			/*
			 * log.info("GET {} → {}", path, res.getStatusCode());
			 */
            
            // Log response to see what Swachvega returns
            log.info("GET {} → status: {} | body: {}",
                     path,
                     res.getStatusCode(),
                     res.getBody() != null
                         ? res.getBody().substring(0,
                             Math.min(300,
                                 res.getBody().length()))
                         : "NULL");

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