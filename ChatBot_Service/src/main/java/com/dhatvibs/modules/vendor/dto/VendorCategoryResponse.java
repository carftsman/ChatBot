package com.dhatvibs.modules.vendor.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorCategoryResponse {
    private String category;
    private String displayName;
    private int    displayOrder;
}