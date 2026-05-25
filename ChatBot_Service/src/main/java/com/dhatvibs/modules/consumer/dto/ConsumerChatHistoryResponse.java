package com.dhatvibs.modules.consumer.dto;


import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ConsumerChatHistoryResponse {
    private UUID             sessionId;
    private String           status;
    private String           resolutionType;
    private Boolean          chatEnabled;
    private String           contextOrderId;
    private LocalDateTime    startedAt;
    private LocalDateTime    endedAt;
    private List<MessageDto> messages;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class MessageDto {
        private UUID          id;
        private String        senderType;
        private String        message;
        private String        messageType;
        private String        intent;
        private LocalDateTime sentAt;
    }
}
