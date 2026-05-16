package com.dhatvibs.modules.rider.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rider_tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    // rider_id from Node.js
    @Column(name = "rider_id")
    private String riderId;

    @Column(name = "category")
    private String category;

    @Column(name = "description",
            columnDefinition = "TEXT")
    private String description;

    // OPEN / IN_PROGRESS / RESOLVED
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