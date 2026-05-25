package com.dhatvibs.modules.consumer.dto;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerResolveRequest {
    @NotNull
    private UUID    sessionId;
    @NotNull
    private Boolean resolved;
}
