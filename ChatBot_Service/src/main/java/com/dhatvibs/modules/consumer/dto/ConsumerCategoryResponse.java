package com.dhatvibs.modules.consumer.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerCategoryResponse {
    private String category;
    private String displayName;
    private int    displayOrder;
}
