package com.dhatvibs.modules.vendor.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendor_tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "vendor_id")
    private String vendorId;

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
