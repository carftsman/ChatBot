package com.dhatvibs.modules.consumer.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerTicketUpdateRequest {
    private String status;
    private String assignedTo;
}