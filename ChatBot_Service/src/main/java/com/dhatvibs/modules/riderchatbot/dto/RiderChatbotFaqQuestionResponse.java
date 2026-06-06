package com.dhatvibs.modules.riderchatbot.dto;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotFaqQuestionResponse {
    private UUID   faqId;
    private String question;
    private String category;
    private String intent;
    private int    displayOrder;
}