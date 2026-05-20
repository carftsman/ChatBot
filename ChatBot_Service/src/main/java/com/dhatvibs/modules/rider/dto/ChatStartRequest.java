
package com.dhatvibs.modules.rider.dto;

import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatStartRequest {
    // Rider JWT token — to call Node.js APIs
    private String riderToken;
    // Optional — order selected by rider
    private String orderId;
}
