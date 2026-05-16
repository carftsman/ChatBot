package com.dhatvibs.modules.rider.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SendOtpRequest {
    @NotBlank(message = "Phone is required")
    private String phone;
}
