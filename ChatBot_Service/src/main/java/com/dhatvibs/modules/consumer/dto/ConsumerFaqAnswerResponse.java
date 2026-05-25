package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerFaqAnswerResponse {
    private UUID    faqId;
    private String  question;
    private String  answer;
    private String  intent;
    private String  category;
    private boolean needsTicket;
    private String  ticketId;
}