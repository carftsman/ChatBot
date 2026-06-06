package com.dhatvibs.modules.riderchatbot.dto;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotResolveRequest {
    @NotNull private UUID    sessionId;
    @NotNull private Boolean resolved;
}