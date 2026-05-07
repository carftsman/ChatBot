package com.dhatvibs.modules.entities.chat;


import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_payments")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CbPayment {

    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "external_payment_id")
    private String externalPaymentId;

    @Column(name = "cb_order_id")
    private UUID cbOrderId;

    @Column(name = "cb_user_id")
    private UUID cbUserId;

    @Column(name = "payment_status")
    private String paymentStatus;

    @Column(name = "payment_method")
    private String paymentMethod;

    @Column(name = "gateway")
    private String gateway;

    @Column(name = "gateway_txn_id")
    private String gatewayTxnId;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "refund_amount")
    private BigDecimal refundAmount;

    @Column(name = "failure_reason", columnDefinition = "TEXT")
    private String failureReason;

    @Column(name = "initiated_at")
    private LocalDateTime initiatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "refund_initiated_at")
    private LocalDateTime refundInitiatedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
