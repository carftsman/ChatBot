package com.dhatvibs.modules.dto.chat;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WebSocketChatRequest {

    @NotNull(message = "SessionId is required")
    private UUID sessionId;

    @NotBlank(message = "Message is required")
    private String message;

    // Sent by frontend for auth since
    // WebSocket headers are tricky
    @NotBlank(message = "UserId is required")
    private String userId;

    @NotBlank(message = "AppId is required")
    private String appId;
}
