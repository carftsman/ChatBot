package com.dhatvibs.modules.service.chat;



/*
 * import java.util.List; import java.util.UUID;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatStartResponse; import
 * com.dhatvibs.modules.dto.chat.WebSocketChatRequest; import
 * com.dhatvibs.modules.dto.chat.WebSocketChatResponse; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage;
 * 
 * public interface ChatService {
 * 
 * // Start session — now accepts optional orderId ChatStartResponse
 * startSession( String externalUserId, String appId, UUID orderId);
 * 
 * // Process message — returns response // Also broadcasts via WebSocket
 * WebSocketChatResponse processMessage( WebSocketChatRequest request);
 * 
 * // End session void endSession(UUID sessionId);
 * 
 * // Get chat history List<CbChatMessage> getHistory(UUID sessionId); }
 */  


import java.util.List;
import java.util.UUID;

import com.dhatvibs.modules.dto.chat.*;

public interface ChatService {

    // Start session with order context
    ChatStartResponse startSession(
            String externalUserId,
            String appId,
            UUID orderId);

    // Called after user taps FAQ answer buttons
    // resolved=true  → close session (RESOLVED)
    // resolved=false → enable free chat (WebSocket)
    SessionStatusResponse resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String externalUserId,
            String appId);

    // Free chat message via WebSocket
    // Only works when chatEnabled = true
    WebSocketChatResponse processMessage(
            WebSocketChatRequest request);

    // End session from free chat
    SessionStatusResponse endSession(
            UUID sessionId,
            String externalUserId,
            String appId);

    // Full history for one session
    ChatHistoryResponse getSessionHistory(
            UUID sessionId);

    // All sessions for a user
    List<ChatHistoryResponse> getAllHistory(
            String externalUserId, String appId);
}
