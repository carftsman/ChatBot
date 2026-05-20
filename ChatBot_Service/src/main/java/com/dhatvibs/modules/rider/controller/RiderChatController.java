package com.dhatvibs.modules.rider.controller;


import com.dhatvibs.modules.rider.dto.*;
import com.dhatvibs.modules.rider.service.RiderChatService;
import com.dhatvibs.modules.rider.service.RiderQueryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import lombok.extern.slf4j.Slf4j;          // ← ADD THIS


import java.util.List;
import java.util.UUID;


@Slf4j
@RestController
@RequestMapping("/rider/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat",
     description = "Rider chat session APIs")
public class RiderChatController {

    private final RiderChatService chatService;
    private final RiderQueryService  riderQueryService; // ← add this


    private String getRiderId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

	/*
	 * @Operation(summary = "1. Start chat session", description =
	 * "Creates session. Pass riderToken from login.")
	 * 
	 * @PostMapping("/start") public ResponseEntity<ChatStartResponse> startSession(
	 * 
	 * @RequestBody(required = false) ChatStartRequest request) { String token =
	 * request != null ? request.getRiderToken() : null; return ResponseEntity.ok(
	 * chatService.startSession( getRiderId(), token)); }
	 */
    
    @PostMapping("/start")
    public ResponseEntity<ChatStartResponse>
            startSession(
            @RequestBody(required = false)
            ChatStartRequest request) {

        log.info("=== /rider/chat/start called ===");
        log.info("Request body: {}", request);

        String token = null;
        String orderId = null;

        if (request != null) {
            token   = request.getRiderToken();
            orderId = request.getOrderId();
        }

        log.info("Extracted → token: {} | orderId: {}",
                 token != null ? "present" : "NULL",
                 orderId);

        return ResponseEntity.ok(
            chatService.startSession(
                getRiderId(), token, orderId));
    }

    @Operation(summary = "2. Resolve or Escalate",
        description = "resolved=true → close. resolved=false → enable free chat")
    @PostMapping("/resolve")
    public ResponseEntity<SessionStatusResponse>
            resolve(
            @Valid @RequestBody
            ResolveRequest request) {
        return ResponseEntity.ok(
            chatService.resolveOrEscalate(
                request.getSessionId(),
                request.getResolved(),
                getRiderId()));
    }

    @Operation(summary = "3. Send message (REST)",
        description = """
            WebSocket: ws://host:8083/ws/rider
            Subscribe: /topic/rider/{sessionId}
            Send to:   /app/rider.message
            """)
    @PostMapping("/message")
    public ResponseEntity<WebSocketChatResponse>
            sendMessage(
            @Valid @RequestBody
            WebSocketChatRequest request) {
        request.setRiderId(getRiderId());
        return ResponseEntity.ok(
            chatService.processMessage(request));
    }

    @MessageMapping("/rider.message")
    public void handleWebSocket(
            WebSocketChatRequest request) {
        chatService.processMessage(request);
    }

    @Operation(summary = "4. End chat session")
    @PostMapping("/end/{sessionId}")
    public ResponseEntity<SessionStatusResponse>
            endSession(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(
            chatService.endSession(sessionId));
    }
    
    

	/*
	 * @Operation(summary = "5. Get one session history")
	 * 
	 * @GetMapping("/history/{sessionId}") public
	 * ResponseEntity<ChatHistoryResponse> getSessionHistory(
	 * 
	 * @PathVariable UUID sessionId) { return ResponseEntity.ok(
	 * chatService.getSessionHistory(sessionId)); }
	 */
    
 // Replace existing history/{sessionId} endpoint
 // and add new history by orderId endpoint

 @Operation(summary = "Get history by sessionId")
 @GetMapping("/history/session/{sessionId}")
 public ResponseEntity<ChatHistoryResponse>
         getSessionHistory(
         @PathVariable UUID sessionId) {
     return ResponseEntity.ok(
         chatService.getSessionHistory(sessionId));
 }

 @Operation(
     summary = "Get history by orderId",
     description = "Pass the orderId to get "
         + "full chat history for that order.")
 @GetMapping("/history/order/{orderId}")
 public ResponseEntity<ChatHistoryResponse>
         getHistoryByOrderId(
         @PathVariable String orderId) {
     return ResponseEntity.ok(
         chatService.getHistoryByOrderId(orderId));
 }

	/*
	 * @Operation(summary = "Get all past sessions")
	 * 
	 * @GetMapping("/history") public ResponseEntity<List<ChatHistoryResponse>>
	 * getAllHistory() { return ResponseEntity.ok(
	 * chatService.getAllHistory(getRiderId())); }
	 */
    
    @Operation(summary = "6. Get all sessions history")
    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryResponse>>
            getAllHistory() {
        return ResponseEntity.ok(
            chatService.getAllHistory(getRiderId()));
    }
    
 // Add to RiderChatController.java

	/*
	 * @Operation(summary = "0. Get recent orders", description =
	 * "First screen — rider taps an order to start chat")
	 * 
	 * @GetMapping("/orders/recent") public ResponseEntity<String> getRecentOrders()
	 * { // Get riderToken from session or request // For now return from
	 * RiderQueryService String riderId = getRiderId(); // Need token — rider must
	 * pass it return ResponseEntity.ok(
	 * "Call /rider/chat/start with riderToken first, " +
	 * "then use /rider/chat/orders/recent"); }
	 */
    
    
    @Operation(
    	    summary = "0. Get recent orders for help center",
    	    description = """
    	        Called first when rider opens help center.
    	        Pass riderToken from verify-otp response.
    	        Returns last 5 deliveries.
    	        Rider taps one order → call /rider/chat/start
    	        with that orderId in body.
    	        """)
    	@GetMapping("/orders/recent")
    	public ResponseEntity<String> getRecentOrders(
    	        @RequestParam String riderToken) {

    	    String orders = riderQueryService
    	        .getRecentOrders(riderToken);

    	    return ResponseEntity.ok(orders);
    	}
}