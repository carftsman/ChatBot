package com.dhatvibs.modules.riderchatbot.dto;
import jakarta.validation.constraints.*;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotWebSocketRequest {
    @NotNull  private UUID   sessionId;
    @NotBlank private String message;
    private String riderId;
}