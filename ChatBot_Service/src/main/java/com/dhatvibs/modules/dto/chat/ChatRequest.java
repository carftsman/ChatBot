package com.dhatvibs.modules.dto.chat;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor 
@Builder
public class ChatRequest {
    @NotBlank(message = "Message is required")
    private String message;
    private UUID sessionId;    // null on first message
}