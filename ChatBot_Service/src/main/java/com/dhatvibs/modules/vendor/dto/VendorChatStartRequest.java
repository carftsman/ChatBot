package com.dhatvibs.modules.vendor.dto;


import lombok.*;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class VendorChatStartRequest {
    private String vendorToken;
    private String orderId;
}
