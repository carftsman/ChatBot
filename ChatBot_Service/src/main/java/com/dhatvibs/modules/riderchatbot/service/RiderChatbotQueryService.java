package com.dhatvibs.modules.riderchatbot.service;

public interface RiderChatbotQueryService {
    // Earnings
    String getEarningsSummary(String token);
    String getDailyEarnings(String token);
    String getWeeklyEarnings(String token);
    String getCashBalance(String token);
    // Orders
    String getOrderHistory(String token);
    String getOrderStats(String token);
    String getOrderDetails(String orderId,
                           String token);
    // Ratings
    String getRatings(String token);
    String getWeeklyPerformance(String token);
    // Slots
    String getActiveSlots(String token);
    String getSlotHistory(String token);
    // Profile
    String getRiderProfile(String token);
    String getWalletBalance(String token);
    String getBankDetails(String token);
    String getDocuments(String token);
    // Incentives & Referral
    String getIncentives(String token);
    String getReferralSummary(String token);
}