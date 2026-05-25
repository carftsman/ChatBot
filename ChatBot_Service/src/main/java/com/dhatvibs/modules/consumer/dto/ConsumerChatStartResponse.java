package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerChatStartResponse {
    private UUID          sessionId;
    private String        status;
    private String        welcomeMessage;
    private Boolean       chatEnabled;
    private String        contextOrderId;
    private LocalDateTime startedAt;
}