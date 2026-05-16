package com.dhatvibs.modules.rider.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginResponse {
    private String token;
    private String message;
}
