package com.dhatvibs.modules.consumer.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerWebSocketRequest {
    @NotNull
    private UUID   sessionId;
    @NotBlank
    private String message;
    private String consumerId;
}