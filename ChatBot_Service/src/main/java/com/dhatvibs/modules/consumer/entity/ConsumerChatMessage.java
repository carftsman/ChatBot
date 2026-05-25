package com.dhatvibs.modules.consumer.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consumer_chat_messages")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "sender_type")
    private String senderType;

    @Column(name = "message",
            columnDefinition = "TEXT")
    private String message;

    @Column(name = "intent")
    private String intent;

    @Column(name = "matched_faq_id")
    private UUID matchedFaqId;

    @Column(name = "message_type")
    private String messageType;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
