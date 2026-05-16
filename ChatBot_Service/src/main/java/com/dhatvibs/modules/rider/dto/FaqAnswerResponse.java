package com.dhatvibs.modules.rider.dto;


import lombok.*;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class FaqAnswerResponse {
    private UUID    faqId;
    private String  question;
    private String  answer;
    private String  intent;
    private String  category;
    private boolean needsTicket;
    private String  ticketId;
}
