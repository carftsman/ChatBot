package com.dhatvibs.modules.entities.chat;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_orders")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CbOrder {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "external_order_id")
    private String externalOrderId;

    @Column(name = "cb_user_id")
    private UUID cbUserId;

    @Column(name = "cb_vendor_id")
    private UUID cbVendorId;

    @Column(name = "cb_rider_id")
    private UUID cbRiderId;

    @Column(name = "order_status")
    private String orderStatus;

    @Column(name = "items_summary", columnDefinition = "TEXT")
    private String itemsSummary;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "delivery_address", columnDefinition = "TEXT")
    private String deliveryAddress;

    @Column(name = "restaurant_name")
    private String restaurantName;

    @Column(name = "estimated_delivery")
    private LocalDateTime estimatedDelivery;

    @Column(name = "actual_delivery")
    private LocalDateTime actualDelivery;

    @Column(name = "cancel_reason", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "cancel_by")
    private String cancelBy;

    @Column(name = "placed_at")
    private LocalDateTime placedAt;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "picked_up_at")
    private LocalDateTime pickedUpAt;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}