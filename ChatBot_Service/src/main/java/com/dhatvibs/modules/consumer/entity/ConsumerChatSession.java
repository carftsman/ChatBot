package com.dhatvibs.modules.consumer.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumer_chat_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "consumer_id")
    private String consumerId;

    @Column(name = "consumer_token",
            columnDefinition = "TEXT")
    private String consumerToken;

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
