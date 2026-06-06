package com.dhatvibs.modules.riderchatbot.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "riderchatbot_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "rider_id")
    private String riderId;

    @Column(name = "rider_token",
            columnDefinition = "TEXT")
    private String riderToken;

    @Column(name = "status")
    private String status;

    @Column(name = "chat_enabled")
    private Boolean chatEnabled;

    @Column(name = "resolution_type")
    private String resolutionType;

    @Column(name = "context_order_id")
    private String contextOrderId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}