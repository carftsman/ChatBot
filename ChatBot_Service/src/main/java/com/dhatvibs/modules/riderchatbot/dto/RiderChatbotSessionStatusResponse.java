package com.dhatvibs.modules.riderchatbot.dto;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotSessionStatusResponse {
    private UUID          sessionId;
    private String        status;
    private Boolean       chatEnabled;
    private String        resolutionType;
    private String        message;
    private LocalDateTime timestamp;
}