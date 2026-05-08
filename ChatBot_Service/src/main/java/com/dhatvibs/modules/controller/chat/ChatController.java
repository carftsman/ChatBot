package com.dhatvibs.modules.controller.chat;

/*
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid;
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatRequest; import
 * com.dhatvibs.modules.dto.chat.ChatResponse; import
 * com.dhatvibs.modules.dto.chat.SessionResponse; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.service.chat.ChatService;
 * 
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/chat")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Chat", description =
 * "Chat APIs for User, Vendor, Rider help centers") public class ChatController
 * {
 * 
 * private final ChatService chatService;
 * 
 * // ── START SESSION ──────────────────────────────
 * 
 * @Operation(summary = "Start a new chat session", description =
 * "Creates a session and returns sessionId + welcome message")
 * 
 * @PostMapping("/start") public ResponseEntity<SessionResponse> startSession(
 * 
 * @RequestHeader("X-User-Id") String userId,
 * 
 * @RequestHeader("X-App-Id") String appId) {
 * 
 * return ResponseEntity.ok( chatService.startSession(userId, appId)); }
 * 
 * // ── SEND MESSAGE ───────────────────────────────
 * 
 * @Operation(summary = "Send a chat message", description =
 * "Send user message, get bot reply based on intent + DB lookup")
 * 
 * @PostMapping("/message") public ResponseEntity<ChatResponse> sendMessage(
 * 
 * @RequestHeader("X-User-Id") String userId,
 * 
 * @RequestHeader("X-App-Id") String appId,
 * 
 * @Valid @RequestBody ChatRequest request) {
 * 
 * return ResponseEntity.ok( chatService.processMessage( request, userId,
 * appId)); }
 * 
 * // ── END SESSION ────────────────────────────────
 * 
 * @Operation(summary = "End a chat session")
 * 
 * @PostMapping("/end/{sessionId}") public ResponseEntity<Map<String, String>>
 * endSession(
 * 
 * @PathVariable UUID sessionId) {
 * 
 * chatService.endSession(sessionId); return ResponseEntity.ok(Map.of(
 * "message", "Session closed successfully", "sessionId",
 * sessionId.toString())); }
 * 
 * // ── CHAT HISTORY ───────────────────────────────
 * 
 * @Operation(summary = "Get all messages in a session")
 * 
 * @GetMapping("/history/{sessionId}") public
 * ResponseEntity<List<CbChatMessage>> getHistory(
 * 
 * @PathVariable UUID sessionId) {
 * 
 * return ResponseEntity.ok( chatService.getHistory(sessionId)); } }
 */  


/*
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid;
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.security.core.Authentication; import
 * org.springframework.security.core.context.SecurityContextHolder; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatRequest; import
 * com.dhatvibs.modules.dto.chat.ChatResponse; import
 * com.dhatvibs.modules.dto.chat.SessionResponse; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.service.chat.ChatService;
 * 
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/chat")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Chat", description = "Chat APIs for User, Vendor, Rider") public
 * class ChatController {
 * 
 * private final ChatService chatService;
 * 
 * // Helper — get userId from Security context private String getUserId() {
 * Authentication auth = SecurityContextHolder
 * .getContext().getAuthentication(); return (String) auth.getPrincipal(); //
 * USR_001 }
 * 
 * // Helper — get appId from Security context private String getAppId() {
 * Authentication auth = SecurityContextHolder
 * .getContext().getAuthentication(); return (String) auth.getCredentials(); //
 * USER }
 * 
 * // ── START SESSION ──────────────────────────
 * 
 * @Operation(summary = "Start a new chat session")
 * 
 * @PostMapping("/start") public ResponseEntity<SessionResponse> startSession()
 * { return ResponseEntity.ok( chatService.startSession( getUserId(),
 * getAppId())); }
 * 
 * // ── SEND MESSAGE ───────────────────────────
 * 
 * @Operation(summary = "Send a chat message")
 * 
 * @PostMapping("/message") public ResponseEntity<ChatResponse> sendMessage(
 * 
 * @Valid @RequestBody ChatRequest request) { return ResponseEntity.ok(
 * chatService.processMessage( request, getUserId(), getAppId())); }
 * 
 * // ── END SESSION ────────────────────────────
 * 
 * @Operation(summary = "End a chat session")
 * 
 * @PostMapping("/end/{sessionId}") public ResponseEntity<Map<String, String>>
 * endSession(
 * 
 * @PathVariable UUID sessionId) { chatService.endSession(sessionId); return
 * ResponseEntity.ok(Map.of( "message", "Session closed successfully",
 * "sessionId", sessionId.toString())); }
 * 
 * // ── CHAT HISTORY ───────────────────────────
 * 
 * @Operation(summary = "Get chat history")
 * 
 * @GetMapping("/history/{sessionId}") public
 * ResponseEntity<List<CbChatMessage>> getHistory(
 * 
 * @PathVariable UUID sessionId) { return ResponseEntity.ok(
 * chatService.getHistory(sessionId)); } }
 */ 



/*
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid;
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.security.core.Authentication; import
 * org.springframework.security.core.context.SecurityContextHolder; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatRequest; import
 * com.dhatvibs.modules.dto.chat.ChatResponse; import
 * com.dhatvibs.modules.dto.chat.SessionResponse; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.service.chat.ChatService;
 * 
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/chat")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Chat", description =
 * "Chat APIs — pass JWT token via Authorize button") public class
 * ChatController {
 * 
 * private final ChatService chatService;
 * 
 * private String getUserId() { Authentication auth = SecurityContextHolder
 * .getContext().getAuthentication(); return (String) auth.getPrincipal(); }
 * 
 * private String getAppId() { Authentication auth = SecurityContextHolder
 * .getContext().getAuthentication(); return (String) auth.getCredentials(); }
 * 
 * @Operation(summary = "Start a new chat session", description =
 * "JWT token required via Authorization header")
 * 
 * @PostMapping("/start") public ResponseEntity<SessionResponse> startSession()
 * { return ResponseEntity.ok( chatService.startSession( getUserId(),
 * getAppId())); }
 * 
 * @Operation(summary = "Send a chat message")
 * 
 * @PostMapping("/message") public ResponseEntity<ChatResponse> sendMessage(
 * 
 * @Valid @RequestBody ChatRequest request) { return ResponseEntity.ok(
 * chatService.processMessage( request, getUserId(), getAppId())); }
 * 
 * @Operation(summary = "End a chat session")
 * 
 * @PostMapping("/end/{sessionId}") public ResponseEntity<Map<String, String>>
 * endSession(
 * 
 * @PathVariable UUID sessionId) { chatService.endSession(sessionId); return
 * ResponseEntity.ok(Map.of( "message", "Session closed successfully",
 * "sessionId", sessionId.toString())); }
 * 
 * @Operation(summary = "Get chat history")
 * 
 * @GetMapping("/history/{sessionId}") public
 * ResponseEntity<List<CbChatMessage>> getHistory(
 * 
 * @PathVariable UUID sessionId) { return ResponseEntity.ok(
 * chatService.getHistory(sessionId)); } }
 */  

/*
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import
 * lombok.RequiredArgsConstructor; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.messaging.handler.annotation.*; import
 * org.springframework.messaging.simp.SimpMessagingTemplate; import
 * org.springframework.security.core.Authentication; import
 * org.springframework.security.core.context.SecurityContextHolder; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import com.dhatvibs.modules.dto.chat.*; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.service.chat.ChatService; import
 * com.dhatvibs.modules.service.chat.OrderService;
 * 
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/chat")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Chat", description = "Help center chat APIs") public class
 * ChatController {
 * 
 * private final ChatService chatService; private final OrderService
 * orderService; private final SimpMessagingTemplate messagingTemplate;
 * 
 * private String getUserId() { return (String) SecurityContextHolder
 * .getContext() .getAuthentication() .getPrincipal(); }
 * 
 * private String getAppId() { return (String) SecurityContextHolder
 * .getContext() .getAuthentication() .getCredentials(); }
 * 
 * // ── 1. RECENT ORDERS ──────────────────────────
 * 
 * @Operation( summary = "Get recent orders for help center", description =
 * "Returns last 5 orders. " + "USER gets customer orders, " +
 * "VENDOR gets store orders, " + "RIDER gets assigned deliveries.")
 * 
 * @GetMapping("/orders/recent") public
 * ResponseEntity<List<RecentOrderResponse>> getRecentOrders() { return
 * ResponseEntity.ok( orderService.getRecentOrders( getUserId(), getAppId())); }
 * 
 * // ── 2. START SESSION ──────────────────────────
 * 
 * @Operation( summary = "Start chat session", description =
 * "Pass orderId if user selected " + "an order. Pass null for general chat.")
 * 
 * @PostMapping("/start") public ResponseEntity<ChatStartResponse> startSession(
 * 
 * @RequestBody(required = false) ChatStartRequest request) {
 * 
 * UUID orderId = (request != null) ? request.getOrderId() : null;
 * 
 * return ResponseEntity.ok( chatService.startSession( getUserId(), getAppId(),
 * orderId)); }
 * 
 * // ── 3. SEND MESSAGE via WebSocket ───────────── // Frontend connects to:
 * ws://localhost:8082/ws/chat // Frontend sends to: /app/chat.message //
 * Frontend subscribes: /topic/session/{sessionId}
 * 
 * @MessageMapping("/chat.message") public void handleWebSocketMessage(
 * WebSocketChatRequest request) { // Process and broadcast to sessionId topic
 * chatService.processMessage(request); }
 * 
 * // ── 4. END SESSION ────────────────────────────
 * 
 * @Operation(summary = "End chat session")
 * 
 * @PostMapping("/end/{sessionId}") public ResponseEntity<Map<String, String>>
 * endSession(
 * 
 * @PathVariable UUID sessionId) { chatService.endSession(sessionId); return
 * ResponseEntity.ok(Map.of( "message", "Session closed", "sessionId",
 * sessionId.toString())); }
 * 
 * // ── 5. CHAT HISTORY ───────────────────────────
 * 
 * @Operation(summary = "Get chat history")
 * 
 * @GetMapping("/history/{sessionId}") public
 * ResponseEntity<List<CbChatMessage>> getHistory(
 * 
 * @PathVariable UUID sessionId) { return ResponseEntity.ok(
 * chatService.getHistory(sessionId)); } }
 */  

/*
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid;
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.messaging.handler.annotation.MessageMapping; import
 * org.springframework.messaging.simp.SimpMessagingTemplate; import
 * org.springframework.security.core.context.SecurityContextHolder; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import com.dhatvibs.modules.dto.chat.*; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.service.chat.*;
 * 
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/chat")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Chat", description = "Help center chat APIs") public class
 * ChatController {
 * 
 * private final ChatService chatService; private final OrderService
 * orderService; private final SimpMessagingTemplate messagingTemplate;
 * 
 * private String getUserId() { return (String) SecurityContextHolder
 * .getContext() .getAuthentication() .getPrincipal(); }
 * 
 * private String getAppId() { return (String) SecurityContextHolder
 * .getContext() .getAuthentication() .getCredentials(); }
 * 
 * // ── 1. RECENT ORDERS ──────────────────────
 * 
 * @Operation( summary = "Get recent orders for help center", description =
 * "Returns last 5 orders. " + "Role is auto-detected from JWT token.")
 * 
 * @GetMapping("/orders/recent") public
 * ResponseEntity<List<RecentOrderResponse>> getRecentOrders() { return
 * ResponseEntity.ok( orderService.getRecentOrders( getUserId(), getAppId())); }
 * 
 * // ── 2. START SESSION ──────────────────────
 * 
 * @Operation( summary = "Start chat session", description =
 * "Pass orderId if user selected " + "an order. orderId is optional.")
 * 
 * @PostMapping("/start") public ResponseEntity<ChatStartResponse> startSession(
 * 
 * @RequestBody(required = false) ChatStartRequest request) {
 * 
 * UUID orderId = (request != null) ? request.getOrderId() : null;
 * 
 * return ResponseEntity.ok( chatService.startSession( getUserId(), getAppId(),
 * orderId)); }
 * 
 * // ── 3. SEND MESSAGE (REST + Swagger visible) ──
 * 
 * @Operation( summary = "Send chat message", description = """ Send a message
 * and get bot reply. For LIVE chat use WebSocket: - Connect:
 * ws://host:8082/ws/chat - Send to: /app/chat.message - Subscribe:
 * /topic/session/{sessionId} This REST endpoint works the same way but without
 * real-time push. """)
 * 
 * @PostMapping("/message") public ResponseEntity<WebSocketChatResponse>
 * sendMessage(
 * 
 * @Valid @RequestBody WebSocketChatRequest request) {
 * 
 * // Set userId and appId from SecurityContext // so frontend doesn't need to
 * send them request.setUserId(getUserId()); request.setAppId(getAppId());
 * 
 * WebSocketChatResponse response = chatService.processMessage(request);
 * 
 * return ResponseEntity.ok(response); }
 * 
 * // ── WebSocket handler (not visible in Swagger) ── // Frontend sends to
 * /app/chat.message
 * 
 * @MessageMapping("/chat.message") public void handleWebSocketMessage(
 * WebSocketChatRequest request) { chatService.processMessage(request); }
 * 
 * // ── 4. END SESSION ────────────────────────
 * 
 * @Operation(summary = "End chat session")
 * 
 * @PostMapping("/end/{sessionId}") public ResponseEntity<Map<String, String>>
 * endSession(
 * 
 * @PathVariable UUID sessionId) { chatService.endSession(sessionId); return
 * ResponseEntity.ok(Map.of( "message", "Session closed", "sessionId",
 * sessionId.toString())); }
 * 
 * // ── 5. CHAT HISTORY ───────────────────────
 * 
 * @Operation(summary = "Get chat history")
 * 
 * @GetMapping("/history/{sessionId}") public
 * ResponseEntity<List<CbChatMessage>> getHistory(
 * 
 * @PathVariable UUID sessionId) { return ResponseEntity.ok(
 * chatService.getHistory(sessionId)); } }
 */  


/*
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import jakarta.validation.Valid;
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.messaging.handler.annotation.MessageMapping; import
 * org.springframework.messaging.simp.SimpMessagingTemplate; import
 * org.springframework.security.core.context.SecurityContextHolder; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import com.dhatvibs.modules.dto.chat.*; import
 * com.dhatvibs.modules.service.chat.*;
 * 
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/api/chat")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Chat", description = "Help center — Blinkit/Zepto style") public
 * class ChatController {
 * 
 * private final ChatService chatService; private final OrderService
 * orderService; private final SimpMessagingTemplate messagingTemplate;
 * 
 * private String getUserId() { return (String) SecurityContextHolder
 * .getContext().getAuthentication() .getPrincipal(); }
 * 
 * private String getAppId() { return (String) SecurityContextHolder
 * .getContext().getAuthentication() .getCredentials(); }
 * 
 * // ── 1. RECENT ORDERS ──────────────────────────
 * 
 * @Operation( summary = "Get recent orders", description =
 * "Shows last 5 orders in help center. " + "USER=customer orders, " +
 * "VENDOR=store orders, " + "RIDER=assigned deliveries.")
 * 
 * @GetMapping("/orders/recent") public
 * ResponseEntity<List<RecentOrderResponse>> getRecentOrders() { return
 * ResponseEntity.ok( orderService.getRecentOrders( getUserId(), getAppId())); }
 * 
 * // ── 2. START SESSION ──────────────────────────
 * 
 * @Operation( summary = "Start chat session", description =
 * "Called when user taps an order. " +
 * "Pass orderId. chatEnabled=false by default. " +
 * "Returns sessionId + welcome message.")
 * 
 * @PostMapping("/start") public ResponseEntity<ChatStartResponse> startSession(
 * 
 * @RequestBody(required = false) ChatStartRequest request) {
 * 
 * UUID orderId = (request != null) ? request.getOrderId() : null;
 * 
 * return ResponseEntity.ok( chatService.startSession( getUserId(), getAppId(),
 * orderId)); }
 * 
 * // ── 3. RESOLVE OR ESCALATE ────────────────────
 * 
 * @Operation( summary = "Issue Resolved / Not Resolved", description = """
 * Called after user reads FAQ answer. resolved=true → ✅ Issue Resolved →
 * session closes immediately → chat stays disabled resolved=false → ❌ Issue Not
 * Resolved → chatEnabled becomes TRUE → free WebSocket chat unlocked """)
 * 
 * @PostMapping("/resolve") public ResponseEntity<SessionStatusResponse>
 * resolveOrEscalate(
 * 
 * @Valid @RequestBody ResolveRequest request) {
 * 
 * return ResponseEntity.ok( chatService.resolveOrEscalate(
 * request.getSessionId(), request.getResolved(), getUserId(), getAppId())); }
 * 
 * // ── 4. SEND MESSAGE (REST visible in Swagger) ─
 * 
 * @Operation( summary = "Send free chat message (REST)", description = """ Only
 * works when chatEnabled=true. For real-time use WebSocket: Connect:
 * ws://host:8082/ws/chat Send to: /app/chat.message Subscribe:
 * /topic/session/{sessionId} Body: { sessionId, message } userId and appId
 * auto-read from JWT. """)
 * 
 * @PostMapping("/message") public ResponseEntity<WebSocketChatResponse>
 * sendMessage(
 * 
 * @Valid @RequestBody WebSocketChatRequest request) {
 * 
 * request.setUserId(getUserId()); request.setAppId(getAppId());
 * 
 * return ResponseEntity.ok( chatService.processMessage(request)); }
 * 
 * // ── WebSocket handler ─────────────────────────
 * 
 * @MessageMapping("/chat.message") public void handleWebSocket(
 * WebSocketChatRequest request) { chatService.processMessage(request); }
 * 
 * // ── 5. END SESSION ────────────────────────────
 * 
 * @Operation( summary = "End chat session", description =
 * "Called when user taps End Chat " +
 * "during free chat. Marks session RESOLVED.")
 * 
 * @PostMapping("/end/{sessionId}") public ResponseEntity<SessionStatusResponse>
 * endSession(
 * 
 * @PathVariable UUID sessionId) {
 * 
 * return ResponseEntity.ok( chatService.endSession( sessionId, getUserId(),
 * getAppId())); }
 * 
 * // ── 6. SESSION HISTORY ────────────────────────
 * 
 * @Operation( summary = "Get history of one session", description =
 * "Returns all messages in a session " +
 * "with timestamps, senderType, messageType.")
 * 
 * @GetMapping("/history/{sessionId}") public
 * ResponseEntity<ChatHistoryResponse> getSessionHistory(
 * 
 * @PathVariable UUID sessionId) {
 * 
 * return ResponseEntity.ok( chatService.getSessionHistory(sessionId)); }
 * 
 * // ── 7. ALL SESSIONS HISTORY ───────────────────
 * 
 * @Operation( summary = "Get all past chat sessions", description =
 * "Returns all sessions for this user " + "with full message history. " +
 * "User can view all past support chats.")
 * 
 * @GetMapping("/history") public ResponseEntity<List<ChatHistoryResponse>>
 * getAllHistory() {
 * 
 * return ResponseEntity.ok( chatService.getAllHistory( getUserId(),
 * getAppId())); } }
 */  


import com.dhatvibs.modules.dto.chat.*;
import com.dhatvibs.modules.service.chat.ChatService;
import com.dhatvibs.modules.service.chat.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(
    name = "Chat",
    description = "Help center — Blinkit/Zepto style")
public class ChatController {

    private final ChatService           chatService;
    private final OrderService          orderService;
    private final SimpMessagingTemplate messagingTemplate;

    private String getUserId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getPrincipal();
    }

    private String getAppId() {
        return (String) SecurityContextHolder
            .getContext()
            .getAuthentication()
            .getCredentials();
    }

    // ── API 1 — RECENT ORDERS ─────────────────────
    @Operation(
        summary = "1. Get recent orders",
        description = """
            First API of help center.
            Returns last 5 orders for this user.
            USER   → orders placed by customer
            VENDOR → orders received by store
            RIDER  → deliveries assigned to rider
            """)
    @GetMapping("/orders/recent")
    public ResponseEntity<List<RecentOrderResponse>>
            getRecentOrders() {
        return ResponseEntity.ok(
            orderService.getRecentOrders(
                getUserId(), getAppId()));
    }

    // ── API 2 — START SESSION ─────────────────────
    @Operation(
        summary = "2. Start chat session",
        description = """
            Called when user taps an order.
            Pass orderId in body.
            chatEnabled = FALSE by default.
            Returns sessionId + welcome message.
            After this → call GET /api/faq/categories
            """)
    @PostMapping("/start")
    public ResponseEntity<ChatStartResponse>
            startSession(
            @RequestBody(required = false)
            ChatStartRequest request) {

        UUID orderId = (request != null)
            ? request.getOrderId() : null;

        return ResponseEntity.ok(
            chatService.startSession(
                getUserId(), getAppId(), orderId));
    }

    // ── API 3 — RESOLVE OR ESCALATE ───────────────
    @Operation(
        summary = "3. Issue Resolved / Not Resolved",
        description = """
            Called after user reads FAQ answer.
            resolved = true  → ✅ Issue Resolved
                             → session closes
                             → chatEnabled stays FALSE
            resolved = false → ❌ Issue Not Resolved
                             → chatEnabled = TRUE
                             → free WebSocket chat unlocked
                             → user can type freely
            """)
    @PostMapping("/resolve")
    public ResponseEntity<SessionStatusResponse>
            resolveOrEscalate(
            @Valid @RequestBody
            ResolveRequest request) {

        return ResponseEntity.ok(
            chatService.resolveOrEscalate(
                request.getSessionId(),
                request.getResolved(),
                getUserId(),
                getAppId()));
    }

    // ── API 4 — SEND FREE CHAT MESSAGE ────────────
    @Operation(
        summary = "4. Send free chat message",
        description = """
            Only works after resolved=false.
            chatEnabled must be TRUE.
            userId and appId auto-read from JWT.
            Body: { sessionId, message }

            For real-time use WebSocket:
              Connect:   ws://host:8082/ws/chat
              Send to:   /app/chat.message
              Subscribe: /topic/session/{sessionId}
              Payload:
              {
                sessionId: "uuid",
                message:   "text",
                userId:    "USR_001",
                appId:     "USER"
              }
            """)
    @PostMapping("/message")
    public ResponseEntity<WebSocketChatResponse>
            sendMessage(
            @Valid @RequestBody
            WebSocketChatRequest request) {

        request.setUserId(getUserId());
        request.setAppId(getAppId());

        return ResponseEntity.ok(
            chatService.processMessage(request));
    }

    // WebSocket handler — not visible in Swagger
    @MessageMapping("/chat.message")
    public void handleWebSocket(
            WebSocketChatRequest request) {
        chatService.processMessage(request);
    }

    // ── API 5 — END SESSION ───────────────────────
    @Operation(
        summary = "5. End chat session",
        description = """
            Called when user taps End Chat in free chat.
            Marks session RESOLVED.
            chatEnabled becomes FALSE.
            WebSocket broadcast sent to close chat.
            """)
    @PostMapping("/end/{sessionId}")
    public ResponseEntity<SessionStatusResponse>
            endSession(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
            chatService.endSession(
                sessionId,
                getUserId(),
                getAppId()));
    }

    // ── API 6 — ONE SESSION HISTORY ───────────────
    @Operation(
        summary = "6. Get history of one session",
        description = """
            Returns all messages in a specific session.
            Includes:
              SYSTEM  → welcome messages
              FAQ     → question + answer from buttons
              FREE_CHAT → free typed messages
            """)
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<ChatHistoryResponse>
            getSessionHistory(
            @PathVariable UUID sessionId) {

        return ResponseEntity.ok(
            chatService.getSessionHistory(sessionId));
    }

    // ── API 7 — ALL SESSIONS HISTORY ─────────────
    @Operation(
        summary = "7. Get all past chat sessions",
        description = """
            Returns all sessions for this user
            with complete message history.
            Ordered by most recent first.
            """)
    @GetMapping("/history")
    public ResponseEntity<List<ChatHistoryResponse>>
            getAllHistory() {

        return ResponseEntity.ok(
            chatService.getAllHistory(
                getUserId(), getAppId()));
    }
}