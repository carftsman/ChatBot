package com.dhatvibs.modules.riderchatbot.service;

import java.util.UUID;

import com.dhatvibs.modules.riderchatbot.dto.*;

public interface RiderChatbotChatService {
    RiderChatbotChatStartResponse startSession(
            String riderId,
            String riderToken,
            String orderId);

    RiderChatbotSessionStatusResponse
        resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String riderId);

    RiderChatbotWebSocketResponse processMessage(
            RiderChatbotWebSocketRequest request);

    RiderChatbotOrderHistoryResponse
        getHistoryByOrderId(
            String orderId, int page, int size);
}