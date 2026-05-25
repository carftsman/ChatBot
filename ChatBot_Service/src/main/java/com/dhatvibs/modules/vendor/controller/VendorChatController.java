package com.dhatvibs.modules.vendor.controller;

import com.dhatvibs.modules.vendor.dto.*;
import com.dhatvibs.modules.vendor.service
        .VendorChatService;
import com.dhatvibs.modules.vendor.service
        .VendorQueryService;
import com.fasterxml.jackson.databind.JsonNode;

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
@RequestMapping("/vendor/chat")
@RequiredArgsConstructor
@Tag(name = "Vendor Chat",
     description = "Vendor chat session APIs")
public class VendorChatController {

    private final VendorChatService  chatService;
    private final VendorQueryService queryService;

    private String getVendorId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

	/*
	 * @Operation(summary = "0. Get recent orders")
	 * 
	 * @GetMapping("/orders/recent") public ResponseEntity<String> getRecentOrders(
	 * 
	 * @RequestParam String vendorToken) { return ResponseEntity.ok(
	 * queryService.getPendingOrders( vendorToken)); }
	 */
    
    @Operation(summary = "0. Get recent orders")
    @GetMapping("/orders/recent")
    public ResponseEntity<List<Map>> getRecentOrders(
            @RequestParam String vendorToken) {
        return ResponseEntity.ok(
            queryService.getPendingOrders(vendorToken));
    }

    @Operation(summary = "1. Start chat session")
    @PostMapping("/start")
    public ResponseEntity<VendorChatStartResponse>
            startSession(
            @RequestBody(required = false)
            VendorChatStartRequest request) {

        String token   = request != null
            ? request.getVendorToken() : null;
        String orderId = request != null
            ? request.getOrderId() : null;

        return ResponseEntity.ok(
            chatService.startSession(
                getVendorId(), token, orderId));
    }

    @Operation(summary = "2. Resolve or Escalate")
    @PostMapping("/resolve")
    public ResponseEntity<VendorSessionStatusResponse>
            resolve(
            @Valid @RequestBody
            VendorResolveRequest request) {
        return ResponseEntity.ok(
            chatService.resolveOrEscalate(
                request.getSessionId(),
                request.getResolved(),
                getVendorId()));
    }

    @Operation(summary = "3. Send message")
    @PostMapping("/message")
    public ResponseEntity<VendorWebSocketResponse>
            sendMessage(
            @Valid @RequestBody
            VendorWebSocketRequest request) {
        request.setVendorId(getVendorId());
        return ResponseEntity.ok(
            chatService.processMessage(request));
    }

    @MessageMapping("/vendor.message")
    public void handleWebSocket(
            VendorWebSocketRequest request) {
        chatService.processMessage(request);
    }

	/*
	 * @Operation(summary = "4. Get history by orderId")
	 * 
	 * @GetMapping("/history/{orderId}") public
	 * ResponseEntity<VendorOrderHistoryResponse> getHistory(
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
    	public ResponseEntity<VendorOrderHistoryResponse>
    	        getHistory(
    	        @PathVariable String orderId,
    	        @RequestParam(defaultValue = "0") int page,
    	        @RequestParam(defaultValue = "20") int size) {

    	    return ResponseEntity.ok(
    	        chatService.getHistoryByOrderId(
    	            orderId, page, size));
    	}
}