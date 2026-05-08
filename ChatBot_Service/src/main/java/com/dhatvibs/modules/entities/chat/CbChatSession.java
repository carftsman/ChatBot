/*
 * package com.dhatvibs.modules.entities.chat;
 * 
 * 
 * import jakarta.persistence.*; import lombok.*; import
 * java.time.LocalDateTime; import java.util.UUID;
 * 
 * @Entity
 * 
 * @Table(name = "cb_chat_sessions")
 * 
 * @Data @NoArgsConstructor @AllArgsConstructor @Builder public class
 * CbChatSession {
 * 
 * @Id
 * 
 * @GeneratedValue(strategy = GenerationType.UUID)
 * 
 * @Column(columnDefinition = "uuid") private UUID id;
 * 
 * @Column(name = "cb_user_id") private UUID cbUserId;
 * 
 * @Column(name = "app_id") private String appId;
 * 
 * @Column(name = "status") private String status;
 * 
 * // orderId user selected before starting chat
 * 
 * @Column(name = "context_order_id", columnDefinition = "uuid") private UUID
 * contextOrderId;
 * 
 * 
 * @Column(name = "started_at") private LocalDateTime startedAt;
 * 
 * @Column(name = "ended_at") private LocalDateTime endedAt; }
 */ 

package com.dhatvibs.modules.entities.chat;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_chat_sessions")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CbChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "cb_user_id")
    private UUID cbUserId;

    @Column(name = "app_id")
    private String appId;

    @Column(name = "status")
    private String status;
    // OPEN / RESOLVED / ESCALATED / ABANDONED

    @Column(name = "context_order_id",
            columnDefinition = "uuid")
    private UUID contextOrderId;

    // FALSE = FAQ buttons only
    // TRUE  = free chat enabled (WebSocket)
    @Column(name = "chat_enabled")
    private Boolean chatEnabled;

    // How session ended
    @Column(name = "resolution_type")
    private String resolutionType;
    // RESOLVED / ESCALATED / ABANDONED

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;
}
