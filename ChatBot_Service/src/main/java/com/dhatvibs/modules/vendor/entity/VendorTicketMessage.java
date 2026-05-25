package com.dhatvibs.modules.vendor.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "vendor_ticket_messages")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorTicketMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "uuid")
    private UUID id;

    @Column(name = "ticket_id")
    private UUID ticketId;

    @Column(name = "sender_type")
    private String senderType;

    @Column(name = "sender_id")
    private String senderId;

    @Column(name = "message",
            columnDefinition = "TEXT")
    private String message;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;
}
