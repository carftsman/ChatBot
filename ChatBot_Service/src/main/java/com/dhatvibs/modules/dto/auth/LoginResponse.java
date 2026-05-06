package com.dhatvibs.modules.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private String token;      // JWT only — appId and userId inside token
    private String message;
}