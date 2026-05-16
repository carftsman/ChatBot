package com.dhatvibs.modules.rider.entities;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "rider_chat_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    // rider_id from Node.js JWT
    @Column(name = "rider_id")
    private String riderId;

    // rider's JWT token — used to call Node.js APIs
    @Column(name = "rider_token",
            columnDefinition = "TEXT")
    private String riderToken;

    @Column(name = "status")
    private String status;
    // OPEN / RESOLVED / ESCALATED

    // false = FAQ buttons only
    // true  = free chat enabled
    @Column(name = "chat_enabled")
    private Boolean chatEnabled;
    
    
 // Add this field
    @Column(name = "context_order_id")
    private String contextOrderId;

    @Column(name = "resolution_type")
    private String resolutionType;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}