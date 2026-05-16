package com.dhatvibs.modules.rider.service;


import java.util.List;

public interface RiderLLMService {

    record LLMResult(
        String intent,
        boolean needsApi,
        String directAnswer) {}

    record ConversationMessage(
        String senderType,
        String message) {}

    LLMResult processMessage(
        String userMessage,
        String riderName,
        List<ConversationMessage> history);
}
