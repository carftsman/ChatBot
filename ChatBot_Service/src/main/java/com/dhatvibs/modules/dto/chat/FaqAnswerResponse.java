package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqAnswerResponse {

    // faqId of the question that was tapped
    private UUID   faqId;

    // The question that was tapped
    // e.g. "Where is my order?"
    private String question;

    // The answer returned to user
    // Static text OR live data from DB
    // e.g. "🛒 Order: EXT_ORD_001
    //        🍽 Spice Garden
    //        📦 Status: OUT FOR DELIVERY
    //        ⏱ ETA: 15 mins"
    private String answer;

    // Intent that was matched
    // e.g. track_order, refund_status
    private String intent;

    // Category of this FAQ
    // e.g. ORDER, PAYMENT
    private String category;

    // TRUE if a support ticket was auto-raised
    // for this answer (e.g. missing_item, fallback)
    private boolean needsTicket;

    // Ticket id if needsTicket = TRUE
    // null if no ticket raised
    private String ticketId;
}