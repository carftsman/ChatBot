package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketChatResponse {
    private String        reply;
    private String        intent;
    private UUID          sessionId;
    private String        senderType;  // BOT
    private String        status;      // OPEN/RESOLVED/ESCALATED
    private LocalDateTime timestamp;
}
