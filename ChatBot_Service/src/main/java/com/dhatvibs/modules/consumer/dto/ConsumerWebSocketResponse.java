package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerWebSocketResponse {
    private String        reply;
    private String        intent;
    private UUID          sessionId;
    private String        senderType;
    private String        status;
    private LocalDateTime timestamp;
}
