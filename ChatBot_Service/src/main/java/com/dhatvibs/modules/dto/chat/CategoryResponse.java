package com.dhatvibs.modules.dto.chat;


import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryResponse {

    // Internal category key
    // e.g. ORDER, PAYMENT, DELIVERY, PRODUCT,
    //      ACCOUNT, SUPPORT, STORE, DISPUTE,
    //      EARNINGS, NAVIGATION, SAFETY, INCENTIVES
    private String category;

    // Display name shown to user as button
    // e.g. "📦 Orders & Tracking"
    // e.g. "💳 Payments & Refunds"
    private String displayName;

    // Order of display in UI
    // Lower number = shown first
    private int displayOrder;
}
