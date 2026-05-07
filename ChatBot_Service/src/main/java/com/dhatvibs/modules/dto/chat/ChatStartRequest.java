package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatStartRequest {
    // Optional — null if user has no order context
    private UUID orderId;
}
