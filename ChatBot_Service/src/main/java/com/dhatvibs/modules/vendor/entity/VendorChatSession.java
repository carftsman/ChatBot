package com.dhatvibs.modules.vendor.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendor_chat_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "vendor_id")
    private String vendorId;

    @Column(name = "vendor_token",
            columnDefinition = "TEXT")
    private String vendorToken;

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
