package com.dhatvibs.modules.rider.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatStartResponse {
    private UUID          sessionId;
    private String        status;
    private String        welcomeMessage;
    private Boolean       chatEnabled;
    private LocalDateTime startedAt;
}
