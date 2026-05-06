package com.dhatvibs.modules.dto.auth;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LogoutResponse {

    private String message;
    private boolean success;
}