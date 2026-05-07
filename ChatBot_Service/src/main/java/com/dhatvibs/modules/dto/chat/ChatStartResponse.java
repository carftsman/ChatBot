package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatStartResponse {
    private UUID          sessionId;
    private String        appId;
    private String        status;
    private String        welcomeMessage;
    private UUID          contextOrderId;
    private LocalDateTime startedAt;
}
