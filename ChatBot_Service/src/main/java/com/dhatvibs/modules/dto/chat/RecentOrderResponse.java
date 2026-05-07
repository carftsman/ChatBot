package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecentOrderResponse {
    private UUID          orderId;
    private String        externalOrderId;
    private String        restaurantName;
    private String        orderStatus;
    private BigDecimal    totalAmount;
    private String        itemsSummary;
    private LocalDateTime placedAt;
}