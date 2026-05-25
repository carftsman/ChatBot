

package com.dhatvibs.modules.consumer.controller;

import com.dhatvibs.modules.consumer.dto.*;
import com.dhatvibs.modules.consumer.service
        .ConsumerChatService;
import com.dhatvibs.modules.consumer.service
        .ConsumerQueryService;
import com.fasterxml.jackson.databind.JsonNode;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler
        .annotation.MessageMapping;
import org.springframework.security.core.context
        .SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/consumer/chat")
@RequiredArgsConstructor
@Tag(name = "Consumer Chat",
     description = "Consumer chat — Swiggy/Zomato style")
public class ConsumerChatController {

    private final ConsumerChatService  chatService;
    private final ConsumerQueryService queryService;

    private String getConsumerId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

	/*
	 * // ── 0. RECENT ORDERS ──────────────────────────
	 * 
	 * @Operation( summary = "0. Get recent orders", description = "First screen. "
	 * + "Pass consumerToken from app login.")
	 * 
	 * @GetMapping("/orders/recent") public ResponseEntity<String> getRecentOrders(
	 * 
	 * @RequestParam String consumerToken) { return ResponseEntity.ok(
	 * queryService.getRecentOrders( consumerToken, getConsumerId())); }
	 */
    
    
    @Operation(
    	    summary = "0. Get recent orders",
    	    description = """
    	        Returns full order objects from Swachvega API.
    	        Each order contains: orderId, orderNumber,
    	        status, totalAmount, orderItems, delivery,
    	        payment and all other fields.
    	        Pass any orderId to /consumer/chat/start
    	        """)
	/*
	 * @GetMapping("/orders/recent") public ResponseEntity<List<JsonNode>>
	 * getRecentOrders(
	 * 
	 * @RequestParam String consumerToken) {
	 * 
	 * List<JsonNode> orders = queryService .getRecentOrders(consumerToken,
	 * getConsumerId());
	 * 
	 * return ResponseEntity.ok(orders); }
	 */
    @GetMapping("/orders/recent")
    public ResponseEntity<List<Map>> getRecentOrders(
            @RequestParam String consumerToken) {
        return ResponseEntity.ok(
            queryService.getRecentOrders(
                consumerToken, getConsumerId()));
    }

    // ── 1. START SESSION ──────────────────────────
    @Operation(
        summary = "1. Start chat session",
        description = """
            Pass orderId from recent orders.
            If open session exists for same orderId,
            it is reused — no new session created.
            This prevents fragmented history.
            """)
    @PostMapping("/start")
    public ResponseEntity<ConsumerChatStartResponse>
            startSession(
            @RequestBody(required = false)
            ConsumerChatStartRequest request) {

        String token   = request != null
            ? request.getConsumerToken() : null;
        String orderId = request != null
            ? request.getOrderId() : null;

        return ResponseEntity.ok(
            chatService.startSession(
                getConsumerId(), token, orderId));
    }

    // ── 2. RESOLVE OR ESCALATE ────────────────────
    @Operation(
        summary = "2. Issue Resolved / Not Resolved",
        description = """
            resolved=true  → session closes
            resolved=false → ticket raised immediately
                           + free chat enabled
            """)
    @PostMapping("/resolve")
    public ResponseEntity<
            ConsumerSessionStatusResponse>
            resolve(
            @Valid @RequestBody
            ConsumerResolveRequest request) {
        return ResponseEntity.ok(
            chatService.resolveOrEscalate(
                request.getSessionId(),
                request.getResolved(),
                getConsumerId()));
    }

    // ── 3. SEND MESSAGE ───────────────────────────
    @Operation(
        summary = "3. Send free chat message",
        description = """
            Only works after resolved=false.
            WebSocket:
              Connect:   ws://host:8082/ws/chat
              Send to:   /app/consumer.message
              Subscribe: /topic/consumer/{sessionId}
            """)
    @PostMapping("/message")
    public ResponseEntity<ConsumerWebSocketResponse>
            sendMessage(
            @Valid @RequestBody
            ConsumerWebSocketRequest request) {
        request.setConsumerId(getConsumerId());
        return ResponseEntity.ok(
            chatService.processMessage(request));
    }

    @MessageMapping("/consumer.message")
    public void handleWebSocket(
            ConsumerWebSocketRequest request) {
        chatService.processMessage(request);
    }

	/*
	 * // ── 4. HISTORY BY ORDERID ─────────────────────
	 * 
	 * @Operation( summary = "4. Get chat history by orderId", description = """
	 * Industry standard — orderId based. Returns ALL sessions for this order merged
	 * into one continuous timeline. Same as Swiggy/Zomato support history. """)
	 * 
	 * @GetMapping("/history/{orderId}") public
	 * ResponseEntity<ConsumerOrderHistoryResponse> getHistory(
	 * 
	 * @PathVariable String orderId) { return ResponseEntity.ok(
	 * chatService.getHistoryByOrderId( orderId)); }
	 */
    
    
    @Operation(
    	    summary = "4. Get chat history by orderId",
    	    description = """
    	        Returns paginated chat history for an order.
    	        All sessions for same order merged into one timeline.
    	        page=0 is first page (default).
    	        size=20 messages per page (default).
    	        """)
    	@GetMapping("/history/{orderId}")
    	public ResponseEntity<ConsumerOrderHistoryResponse>
    	        getHistory(
    	        @PathVariable String orderId,
    	        @RequestParam(defaultValue = "0") int page,
    	        @RequestParam(defaultValue = "20") int size) {

    	    return ResponseEntity.ok(
    	        chatService.getHistoryByOrderId(
    	            orderId, page, size));
    	}
}