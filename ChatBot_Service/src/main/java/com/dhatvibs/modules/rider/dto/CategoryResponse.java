package com.dhatvibs.modules.rider.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CategoryResponse {
    private String category;
    private String displayName;
    private int    displayOrder;
}