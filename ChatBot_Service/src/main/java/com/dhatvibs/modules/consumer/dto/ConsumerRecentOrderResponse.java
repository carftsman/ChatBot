package com.dhatvibs.modules.consumer.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerRecentOrderResponse {
    private String orderId;
    private String orderNumber;
    private String status;
    private String totalAmount;
    private String storeName;
    private String placedAt;
}
