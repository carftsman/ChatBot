package com.dhatvibs.modules.vendor.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorWebSocketRequest {
    @NotNull
    private UUID   sessionId;
    @NotBlank
    private String message;
    private String vendorId;
}