package com.dhatvibs.modules.dto.auth;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ErrorResponse {

    private int           statusCode;
    private String        error;
    private String        message;
    private LocalDateTime timestamp;
    private String        path;
}
