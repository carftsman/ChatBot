package com.dhatvibs.modules.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class ChatResponse {
    private String reply;
    private String intent;
    private UUID   sessionId;
    private String status;     // RESOLVED / ESCALATED / OPEN
    private LocalDateTime timestamp;
}
