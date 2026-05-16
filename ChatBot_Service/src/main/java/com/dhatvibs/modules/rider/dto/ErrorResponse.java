package com.dhatvibs.modules.rider.dto;


import lombok.*;
import java.time.LocalDateTime;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ErrorResponse {
    private int           statusCode;
    private String        error;
    private String        message;
    private LocalDateTime timestamp;
    private String        path;
}
