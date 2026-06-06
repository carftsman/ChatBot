package com.dhatvibs.modules.rider.service;


import com.dhatvibs.modules.rider.dto.*;
import java.util.List;
import java.util.UUID;

public interface RiderChatService {
    ChatStartResponse    startSession(String riderId, String riderToken,String orderId);
    SessionStatusResponse resolveOrEscalate(UUID sessionId, boolean resolved, String riderId);
    WebSocketChatResponse processMessage(WebSocketChatRequest request);
    SessionStatusResponse endSession(UUID sessionId);
    ChatHistoryResponse   getSessionHistory(UUID sessionId);
    List<ChatHistoryResponse> getAllHistory(String riderId);
 // Add to RiderChatService.java
    ChatHistoryResponse getHistoryByOrderId(
            String orderId,
            String riderId);
}
