package com.dhatvibs.modules.entities.chat;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_tickets")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CbTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "cb_user_id")
    private UUID cbUserId;

    @Column(name = "cb_order_id")
    private UUID cbOrderId;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "category")
    private String category;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}