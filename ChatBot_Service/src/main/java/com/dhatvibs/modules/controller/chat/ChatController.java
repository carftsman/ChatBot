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



import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.dhatvibs.modules.dto.chat.ChatRequest;
import com.dhatvibs.modules.dto.chat.ChatResponse;
import com.dhatvibs.modules.dto.chat.SessionResponse;
import com.dhatvibs.modules.entities.chat.CbChatMessage;
import com.dhatvibs.modules.service.chat.ChatService;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Tag(name = "Chat",
     description = "Chat APIs — pass JWT token via Authorize button")
public class ChatController {

    private final ChatService chatService;

    private String getUserId() {
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        return (String) auth.getPrincipal();
    }

    private String getAppId() {
        Authentication auth = SecurityContextHolder
            .getContext().getAuthentication();
        return (String) auth.getCredentials();
    }

    @Operation(summary = "Start a new chat session",
        description = "JWT token required via Authorization header")
    @PostMapping("/start")
    public ResponseEntity<SessionResponse> startSession() {
        return ResponseEntity.ok(
            chatService.startSession(
                getUserId(), getAppId()));
    }

    @Operation(summary = "Send a chat message")
    @PostMapping("/message")
    public ResponseEntity<ChatResponse> sendMessage(
            @Valid @RequestBody ChatRequest request) {
        return ResponseEntity.ok(
            chatService.processMessage(
                request, getUserId(), getAppId()));
    }

    @Operation(summary = "End a chat session")
    @PostMapping("/end/{sessionId}")
    public ResponseEntity<Map<String, String>> endSession(
            @PathVariable UUID sessionId) {
        chatService.endSession(sessionId);
        return ResponseEntity.ok(Map.of(
            "message", "Session closed successfully",
            "sessionId", sessionId.toString()));
    }

    @Operation(summary = "Get chat history")
    @GetMapping("/history/{sessionId}")
    public ResponseEntity<List<CbChatMessage>> getHistory(
            @PathVariable UUID sessionId) {
        return ResponseEntity.ok(
            chatService.getHistory(sessionId));
    }
}