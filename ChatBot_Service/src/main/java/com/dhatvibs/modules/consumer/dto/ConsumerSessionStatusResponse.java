package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerSessionStatusResponse {
    private UUID          sessionId;
    private String        status;
    private Boolean       chatEnabled;
    private String        resolutionType;
    private String        message;
    private LocalDateTime timestamp;
}
