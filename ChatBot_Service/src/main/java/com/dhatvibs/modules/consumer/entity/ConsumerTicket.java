package com.dhatvibs.modules.consumer.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumer_tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "consumer_id")
    private String consumerId;

    @Column(name = "order_id")
    private String orderId;

    @Column(name = "category")
    private String category;

    @Column(name = "description",
            columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "assigned_to")
    private String assignedTo;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}