package com.dhatvibs.modules.riderchatbot.dto;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RiderChatbotCategoryResponse {
    private String category;
    private String displayName;
    private int    displayOrder;
}