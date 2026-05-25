package com.dhatvibs.modules.vendor.dto;

import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorFaqQuestionResponse {
    private UUID   faqId;
    private String question;
    private String category;
    private String intent;
    private int    displayOrder;
}