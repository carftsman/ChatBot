package com.dhatvibs.modules.rider.dto;


import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FaqQuestionResponse {
    private UUID   faqId;
    private String question;
    private String category;
    private String intent;
    private int    displayOrder;
}