package com.dhatvibs.modules.consumer.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerChatStartRequest {
    private String consumerToken;
    private String orderId;
}
