package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminReplyResponse {
    private UUID          messageId;
    private UUID          ticketId;
    private String        message;
    private String        senderType;
    private LocalDateTime sentAt;
}
