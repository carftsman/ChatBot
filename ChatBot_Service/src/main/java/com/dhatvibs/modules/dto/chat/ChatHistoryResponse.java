package com.dhatvibs.modules.dto.chat;


import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatHistoryResponse {

    private UUID          sessionId;
    private String        appId;
    private String        status;
    private String        resolutionType;
    private Boolean       chatEnabled;
    private UUID          contextOrderId;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
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