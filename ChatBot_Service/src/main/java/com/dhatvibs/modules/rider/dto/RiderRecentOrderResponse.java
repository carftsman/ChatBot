package com.dhatvibs.modules.rider.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderRecentOrderResponse {
    private String orderId;
    private String status;
    private String amount;
    private String customerName;
    private String deliveryAddress;
    private String placedAt;
}
