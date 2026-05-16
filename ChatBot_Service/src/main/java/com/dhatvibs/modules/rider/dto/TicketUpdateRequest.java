package com.dhatvibs.modules.rider.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class TicketUpdateRequest {
    private String status;
    private String assignedTo;
}
