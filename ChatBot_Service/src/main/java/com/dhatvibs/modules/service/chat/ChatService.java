package com.dhatvibs.modules.service.chat;


/*
 * import java.util.*;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatRequest; import
 * com.dhatvibs.modules.dto.chat.ChatResponse; import
 * com.dhatvibs.modules.dto.chat.SessionResponse; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage;
 * 
 * public interface ChatService { SessionResponse startSession(String
 * externalUserId, String appId); ChatResponse processMessage(ChatRequest
 * request, String externalUserId, String appId); void endSession(UUID
 * sessionId); List<CbChatMessage> getHistory(UUID sessionId); }
 */


import java.util.List;
import java.util.UUID;

import com.dhatvibs.modules.dto.chat.ChatStartResponse;
import com.dhatvibs.modules.dto.chat.WebSocketChatRequest;
import com.dhatvibs.modules.dto.chat.WebSocketChatResponse;
import com.dhatvibs.modules.entities.chat.CbChatMessage;

public interface ChatService {

    // Start session — now accepts optional orderId
    ChatStartResponse startSession(
            String externalUserId,
            String appId,
            UUID orderId);

    // Process message — returns response
    // Also broadcasts via WebSocket
    WebSocketChatResponse processMessage(
            WebSocketChatRequest request);

    // End session
    void endSession(UUID sessionId);

    // Get chat history
    List<CbChatMessage> getHistory(UUID sessionId);
}
