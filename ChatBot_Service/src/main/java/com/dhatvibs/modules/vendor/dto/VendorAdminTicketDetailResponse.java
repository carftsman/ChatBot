package com.dhatvibs.modules.vendor.dto;

import com.dhatvibs.modules.vendor.entity.*;
import lombok.*;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorAdminTicketDetailResponse {
    private VendorTicket            ticket;
    private List<VendorTicketMessage> messages;
    private String                  chatHistory;
}