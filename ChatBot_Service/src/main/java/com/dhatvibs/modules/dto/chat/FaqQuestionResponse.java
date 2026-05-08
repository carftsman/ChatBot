package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaqQuestionResponse {

    // Unique FAQ id from cb_faqs table
    // Frontend passes this to /api/faq/answer/{faqId}
    private UUID   faqId;

    // Question text shown as button
    // e.g. "Where is my order?"
    // e.g. "Cancel my order"
    private String question;

    // Which category this belongs to
    // e.g. ORDER, PAYMENT, DELIVERY
    private String category;

    // Intent key for this question
    // e.g. track_order, cancel_order, refund_status
    private String intent;

    // Display order within the category
    private int displayOrder;
}
