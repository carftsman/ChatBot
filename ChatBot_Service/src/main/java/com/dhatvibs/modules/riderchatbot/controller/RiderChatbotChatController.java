package com.dhatvibs.modules.riderchatbot.controller;

import com.dhatvibs.modules.riderchatbot.client.RiderChatbotApiClient;
import com.dhatvibs.modules.riderchatbot.dto.*;
import com.dhatvibs.modules.riderchatbot.service
        .RiderChatbotChatService;
import com.dhatvibs.modules.riderchatbot.service
        .RiderChatbotQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler
        .annotation.MessageMapping;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/riderchatbot/chat")
@RequiredArgsConstructor
@Tag(name = "Rider Chatbot Chat",
     description = "Rider chat session APIs")
public class RiderChatbotChatController {

    private final RiderChatbotChatService
                                chatService;
    private final RiderChatbotQueryService
                                queryService;
    
    private final RiderChatbotApiClient apiClient;
    private final ObjectMapper objectMapper =
        new ObjectMapper();

    private String getRiderId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

	
    
	/*
	 * @GetMapping("/orders/recent") public ResponseEntity<List<Map>>
	 * getRecentOrders(
	 * 
	 * @RequestParam String riderToken) {
	 * 
	 * log.info("=== getRecentOrders called ===");
	 * 
	 * // Try both endpoints and log both responses JsonNode d1 =
	 * apiClient.getOrderHistory(riderToken);
	 * log.info("delivered orders response: {}", d1 != null ? d1.toPrettyString()
	 * .substring(0, Math.min(500, d1.toPrettyString().length())) : "NULL");
	 * 
	 * // Also try profile orders history JsonNode d2 =
	 * apiClient.getDeliveredOrdersHistory( riderToken);
	 * log.info("profile orders history response: {}", d2 != null ?
	 * d2.toPrettyString() .substring(0, Math.min(500,
	 * d2.toPrettyString().length())) : "NULL");
	 * 
	 * // Use whichever has data JsonNode d = d1; if (d == null || !hasOrders(d)) d
	 * = d2;
	 * 
	 * if (d == null) return ResponseEntity.ok(List.of());
	 * 
	 * JsonNode array = extractArray(d); if (array == null || array.size() == 0)
	 * return ResponseEntity.ok(List.of());
	 * 
	 * List<Map> orders = new ArrayList<>(); int count = 0; for (JsonNode o : array)
	 * { if (count++ >= 10) break; orders.add(objectMapper .convertValue(o,
	 * Map.class)); } return ResponseEntity.ok(orders); }
	 * 
	 * private boolean hasOrders(JsonNode d) { return (d.has("orders") &&
	 * d.path("orders").isArray() && d.path("orders").size() > 0) || (d.has("data")
	 * && d.path("data").isArray() && d.path("data").size() > 0) || (d.isArray() &&
	 * d.size() > 0); }
	 * 
	 * private JsonNode extractArray(JsonNode d) { if (d.has("orders") &&
	 * d.path("orders").isArray()) return d.path("orders"); if (d.has("data") &&
	 * d.path("data").isArray()) return d.path("data"); if (d.isArray()) return d;
	 * return null; }
	 */
    @GetMapping("/orders/recent")
    public ResponseEntity<List<Map>> getRecentOrders(
            @RequestParam String riderToken) {

        // /api/orders/delivered → { success, count, orders: [...] }
        JsonNode d = apiClient.getOrderHistory(riderToken);

        log.info("getRecentOrders: {}",
                 d != null ? d.toPrettyString()
                     .substring(0, Math.min(300,
                         d.toPrettyString().length()))
                           : "NULL");

        if (d == null)
            return ResponseEntity.ok(List.of());

        // orders is the correct key
        JsonNode array = d.path("orders");

        if (array.isMissingNode()
                || !array.isArray()
                || array.size() == 0)
            return ResponseEntity.ok(List.of());

        List<Map> orders = new ArrayList<>();
        int count = 0;
        for (JsonNode o : array) {
            if (count++ >= 10) break;
            orders.add(objectMapper
                .convertValue(o, Map.class));
        }

        log.info("Returning {} orders",
                 orders.size());
        return ResponseEntity.ok(orders);
    }

    @Operation(summary = "1. Start chat session")
    @PostMapping("/start")
    public ResponseEntity<
            RiderChatbotChatStartResponse>
            startSession(
            @RequestBody(required = false)
            RiderChatbotChatStartRequest request) {

        String token   = request != null
            ? request.getRiderToken() : null;
        String orderId = request != null
            ? request.getOrderId() : null;

        return ResponseEntity.ok(
            chatService.startSession(
                getRiderId(), token, orderId));
    }

    @Operation(
        summary = "2. Issue Resolved / Not Resolved")
    @PostMapping("/resolve")
    public ResponseEntity<
            RiderChatbotSessionStatusResponse>
            resolve(
            @Valid @RequestBody
            RiderChatbotResolveRequest request) {
        return ResponseEntity.ok(
            chatService.resolveOrEscalate(
                request.getSessionId(),
                request.getResolved(),
                getRiderId()));
    }

    @Operation(summary = "3. Send free chat message")
    @PostMapping("/message")
    public ResponseEntity<
            RiderChatbotWebSocketResponse>
            sendMessage(
            @Valid @RequestBody
            RiderChatbotWebSocketRequest request) {
        request.setRiderId(getRiderId());
        return ResponseEntity.ok(
            chatService.processMessage(request));
    }

    @MessageMapping("/riderchatbot.message")
    public void handleWebSocket(
            RiderChatbotWebSocketRequest request) {
        chatService.processMessage(request);
    }

    @Operation(
        summary = "4. Get chat history by orderId",
        description = "Paginated. page=0, size=20 default.")
    @GetMapping("/history/{orderId}")
    public ResponseEntity<
            RiderChatbotOrderHistoryResponse>
            getHistory(
            @PathVariable String orderId,
            @RequestParam(defaultValue = "0")
                int page,
            @RequestParam(defaultValue = "20")
                int size) {
        return ResponseEntity.ok(
            chatService.getHistoryByOrderId(
                orderId, page, size));
    }
}