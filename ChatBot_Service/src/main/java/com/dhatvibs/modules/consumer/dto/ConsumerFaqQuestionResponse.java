package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerFaqQuestionResponse {
    private UUID   faqId;
    private String question;
    private String category;
    private String intent;
    private int    displayOrder;
}