package com.dhatvibs.modules.dto.chat;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.*;

@Data 
@NoArgsConstructor 
@AllArgsConstructor 
@Builder
public class SessionResponse {
    private UUID   sessionId;
    private String appId;
    private String status;
    private LocalDateTime startedAt;
}