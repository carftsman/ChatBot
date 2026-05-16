package com.dhatvibs.modules.rider.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ResolveRequest {
    @NotNull
    private UUID    sessionId;
    @NotNull
    private Boolean resolved;
}
