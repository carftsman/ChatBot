package com.dhatvibs.modules.riderchatbot.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotChatStartRequest {
    private String riderToken;
    private String orderId;
}