package com.dhatvibs.modules.riderchatbot.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotWebSocketResponse {
    private String        reply;
    private String        intent;
    private UUID          sessionId;
    private String        senderType;
    private String        status;
    private LocalDateTime timestamp;
}