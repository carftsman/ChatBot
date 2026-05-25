package com.dhatvibs.modules.consumer.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminReplyRequest {
    @NotNull
    private UUID   ticketId;
    @NotBlank
    private String message;
    private String adminId;
}