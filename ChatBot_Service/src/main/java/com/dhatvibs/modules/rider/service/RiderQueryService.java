
package com.dhatvibs.modules.rider.service;

public interface RiderQueryService {

    String getEarningsSummary(String token);
    String getDailyEarnings(String token);
    String getWeeklyEarnings(String token);
    String getCashBalance(String token);
    //String getOrderStats(String token);
    String getOrderDetails(String orderId);
    String getOrderHistory(String token);
    String getRecentOrders(String token);  // ← ADD THIS
    String getRatings(String token);
    String getWeeklyPerformance(String token);
    String getRiderProfile(String token);
    String getWalletBalance(String token);
}