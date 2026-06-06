/*
 * package com.dhatvibs.modules.vendor.client;
 * 
 * import com.fasterxml.jackson.databind.JsonNode; import
 * com.fasterxml.jackson.databind.ObjectMapper;
 * 
 * import jakarta.annotation.PostConstruct; import lombok.extern.slf4j.Slf4j;
 * import org.springframework.beans.factory.annotation .Value; import
 * org.springframework.http.*; import org.springframework.stereotype.Component;
 * import org.springframework.web.client.RestTemplate;
 * 
 * @Slf4j
 * 
 * @Component public class VendorApiClient {
 * 
 * @Value("${vendor.api.base-url}") private String baseUrl;
 * 
 * private final RestTemplate restTemplate = new RestTemplate(); private final
 * ObjectMapper objectMapper = new ObjectMapper();
 * 
 * 
 * 
 * @PostConstruct public void init() {
 * log.info("=== VendorApiClient baseUrl: {} ===", baseUrl); }
 * 
 * // ── ORDERS ────────────────────────────────────
 * 
 * public JsonNode getPendingOrders(String token) {
 * 
 * return get("/api/merchant/orders/pending", token);
 * 
 * 
 * // Try the correct merchant order path JsonNode result = get(
 * "/api/merchant/orders/pending", token);
 * 
 * if (result == null) { // Try alternative path result = get(
 * "/api/merchant/dashboard/store/orders-list", token); } return result;
 * 
 * 
 * }
 * 
 * public JsonNode getDeliveredOrders(String token) { return get(
 * "/api/merchant/orders/delivered", token); }
 * 
 * public JsonNode getCancelledOrders(String token) { return get(
 * "/api/merchant/orders/cancelled", token); }
 * 
 * public JsonNode getOrderById( String orderId, String token) { return get(
 * "/api/merchant/dashboard/store/orders/" + orderId, token); }
 * 
 * // ── STORE ─────────────────────────────────────
 * 
 * public JsonNode getStoreDetails(String token) { return get(
 * "/api/merchant/dashboard/store", token); }
 * 
 * // ── WALLET / PAYMENT ──────────────────────────
 * 
 * public JsonNode getWalletBalance(String token) { return get(
 * "/api/merchant/entitlement", token); }
 * 
 * public JsonNode getTransactionHistory( String token) { return get(
 * "/api/merchant/entitlement/transactions", token); }
 * 
 * // ── GENERIC ───────────────────────────────────
 * 
 * private JsonNode get( String path, String token) { try { HttpEntity<String>
 * entity = new HttpEntity<>(jsonHeaders(token)); ResponseEntity<String> res =
 * restTemplate.exchange( baseUrl + path, HttpMethod.GET, entity, String.class);
 * 
 * 
 * log.info("GET {} → {}", path, res.getStatusCode());
 * 
 * 
 * // Log response to see what Swachvega returns
 * log.info("GET {} → status: {} | body: {}", path, res.getStatusCode(),
 * res.getBody() != null ? res.getBody().substring(0, Math.min(300,
 * res.getBody().length())) : "NULL");
 * 
 * if (res.getStatusCode() .is2xxSuccessful() && res.getBody() != null) { return
 * objectMapper.readTree( res.getBody()); } } catch (Exception e) {
 * log.error("GET {} failed: {}", path, e.getMessage()); } return null; }
 * 
 * private HttpHeaders jsonHeaders(String token) { HttpHeaders h = new
 * HttpHeaders(); h.setContentType( MediaType.APPLICATION_JSON); if (token !=
 * null && !token.isBlank()) h.setBearerAuth(token); return h; } }
 */



package com.dhatvibs.modules.vendor.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation
        .Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;

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
        log.info("VendorApiClient baseUrl = {}",
                 baseUrl);
    }

    // ── ORDERS ────────────────────────────────────

    // Confirmed working in Insomnia
    public JsonNode getPendingOrders(String token) {
        return get(
            "/api/merchant/orders/pending", token);
    }

    // Confirmed working in Insomnia
    public JsonNode getCancelledOrders(String token) {
        return get(
            "/api/merchant/orders/cancelled", token);
    }

    // Use dashboard orders-list for delivered
    public JsonNode getDeliveredOrders(String token) {
        return get(
            "/api/merchant/dashboard/store"
            + "/orders-list", token);
    }

    // Use dashboard order detail endpoint
    public JsonNode getOrderById(
            String orderId, String token) {
        // Try dashboard endpoint first
        JsonNode result = get(
            "/api/merchant/dashboard/store/orders/"
            + orderId, token);
        if (result != null) return result;

        // Fallback to orders-list with search
        return get(
            "/api/merchant/dashboard/store"
            + "/orders-list", token);
    }

    // Get orders by store - confirmed working
    public JsonNode getStoreOrders(
            String storeId, String token) {
        return get(
            "/api/orders/store/" + storeId
            + "/counts", token);
    }

    // ── STORE ─────────────────────────────────────

    // Confirmed working — prod URL
    public JsonNode getStoreDetails(String token) {
        return get(
            "/api/merchant/dashboard/store", token);
    }

    // Toggle store open/close
    public JsonNode toggleStoreStatus(String token) {
        return post(
            "/api/merchant/store/open/toggle",
            token, null);
    }

    // ── WALLET / PAYMENT ──────────────────────────

    // Confirmed working in Insomnia
    public JsonNode getWalletBalance(String token) {
        return get(
            "/api/merchant/entitlement", token);
    }

    // Confirmed working in Insomnia
    public JsonNode getTransactionHistory(
            String token) {
        return get(
            "/api/merchant/entitlement/transactions",
            token);
    }

    // ── READY FOR PICKUP ──────────────────────────

    public JsonNode getReadyForPickup(String token) {
        return get(
            "/api/merchant/orders/ready-for-pickup",
            token);
    }

    // ── GENERIC GET ───────────────────────────────

    private JsonNode get(
            String path, String token) {
        try {
            HttpEntity<String> entity =
                new HttpEntity<>(jsonHeaders(token));

            String url = baseUrl + path;
            log.info("GET {}", url);

            ResponseEntity<String> res =
                restTemplate.exchange(
                    url, HttpMethod.GET,
                    entity, String.class);

            log.info("GET {} → status: {} | body: {}",
                     path,
                     res.getStatusCode(),
                     res.getBody() != null
                         ? res.getBody().substring(0,
                             Math.min(300,
                                 res.getBody().length()))
                         : "NULL");

            if (res.getStatusCode().is2xxSuccessful()
                    && res.getBody() != null) {
                return objectMapper.readTree(
                    res.getBody());
            }

            log.error("GET {} failed: {} | {}",
                      path,
                      res.getStatusCode(),
                      res.getBody());

        } catch (Exception e) {
            log.error("GET {} error: {}",
                      path, e.getMessage());
        }
        return null;
    }

    private JsonNode post(
            String path, String token,
            Object body) {
        try {
            HttpEntity<Object> entity =
                new HttpEntity<>(body,
                    jsonHeaders(token));

            ResponseEntity<String> res =
                restTemplate.exchange(
                    baseUrl + path,
                    HttpMethod.POST,
                    entity, String.class);

            log.info("POST {} → {}",
                     path, res.getStatusCode());

            if (res.getStatusCode().is2xxSuccessful()
                    && res.getBody() != null) {
                return objectMapper.readTree(
                    res.getBody());
            }
        } catch (Exception e) {
            log.error("POST {} error: {}",
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