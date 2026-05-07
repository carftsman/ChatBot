package com.dhatvibs.modules.entities.chat;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cb_chat_messages")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CbChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "sender_type")
    private String senderType;

    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "intent")
    private String intent;

    @Column(name = "matched_faq_id")
    private UUID matchedFaqId;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
