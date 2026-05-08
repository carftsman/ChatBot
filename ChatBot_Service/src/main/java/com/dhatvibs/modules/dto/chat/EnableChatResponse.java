package com.dhatvibs.modules.dto.chat;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class EnableChatResponse {
    private UUID    sessionId;
    private Boolean chatEnabled;
    private String  message;
    // WebSocket topic frontend subscribes to
    private String  webSocketTopic;
}