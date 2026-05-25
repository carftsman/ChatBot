package com.dhatvibs.modules.vendor.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorOrderHistoryResponse {
    private String           orderId;
    private String           latestStatus;
    
 // Pagination info
    private int              page;
    private int              size;
    private long             totalMessages;
    private int              totalPages;
    private boolean          hasNext;
    private boolean          hasPrevious;
    private List<MessageDto> messages;

    @Data @NoArgsConstructor
    @AllArgsConstructor @Builder
    public static class MessageDto {
        private UUID          id;
        private String        senderType;
        private String        message;
        private String        messageType;
        private String        intent;
        private LocalDateTime sentAt;
        private String        sessionId;
    }
}