package com.dhatvibs.modules.dto.chat;


import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ResolveRequest {

    @NotNull(message = "SessionId is required")
    private UUID sessionId;

    // true  = Issue Resolved → close session
    // false = Issue Not Resolved → enable free chat
    @NotNull(message = "resolved flag is required")
    private Boolean resolved;
}
