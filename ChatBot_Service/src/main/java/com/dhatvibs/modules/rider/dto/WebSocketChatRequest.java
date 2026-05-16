package com.dhatvibs.modules.rider.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class WebSocketChatRequest {
    @NotNull(message = "SessionId is required")
    private UUID   sessionId;
    @NotBlank(message = "Message is required")
    private String message;
    private String riderId;
}
