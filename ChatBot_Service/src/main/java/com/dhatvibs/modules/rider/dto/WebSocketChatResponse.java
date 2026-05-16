package com.dhatvibs.modules.rider.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WebSocketChatResponse {
    private String        reply;
    private String        intent;
    private UUID          sessionId;
    private String        senderType;
    private String        status;
    private LocalDateTime timestamp;
}
