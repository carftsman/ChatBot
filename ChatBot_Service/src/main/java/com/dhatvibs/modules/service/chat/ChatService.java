package com.dhatvibs.modules.service.chat;


import java.util.*;

import com.dhatvibs.modules.dto.chat.ChatRequest;
import com.dhatvibs.modules.dto.chat.ChatResponse;
import com.dhatvibs.modules.dto.chat.SessionResponse;
import com.dhatvibs.modules.entities.chat.CbChatMessage;

public interface ChatService {
    SessionResponse startSession(String externalUserId, String appId);
    ChatResponse    processMessage(ChatRequest request, String externalUserId, String appId);
    void            endSession(UUID sessionId);
    List<CbChatMessage> getHistory(UUID sessionId);
}
