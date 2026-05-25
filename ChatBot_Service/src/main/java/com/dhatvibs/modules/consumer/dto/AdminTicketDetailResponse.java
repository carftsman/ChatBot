package com.dhatvibs.modules.consumer.dto;


import com.dhatvibs.modules.consumer.entity
        .ConsumerTicket;
import com.dhatvibs.modules.consumer.entity
        .ConsumerTicketMessage;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class AdminTicketDetailResponse {
    private ConsumerTicket            ticket;
    private List<ConsumerTicketMessage> messages;
    private String                    chatHistory;
}