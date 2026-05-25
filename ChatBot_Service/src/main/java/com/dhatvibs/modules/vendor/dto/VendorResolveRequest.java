package com.dhatvibs.modules.vendor.dto;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorResolveRequest {
    @NotNull
    private UUID    sessionId;
    @NotNull
    private Boolean resolved;
}