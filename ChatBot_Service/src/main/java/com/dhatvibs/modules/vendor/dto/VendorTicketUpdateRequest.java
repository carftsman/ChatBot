package com.dhatvibs.modules.vendor.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorTicketUpdateRequest {
    private String status;
    private String assignedTo;
}