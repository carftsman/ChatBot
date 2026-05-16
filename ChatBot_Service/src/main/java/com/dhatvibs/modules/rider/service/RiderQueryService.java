/*
 * package com.dhatvibs.modules.rider.service;
 * 
 * 
 * public interface RiderQueryService { String getEarningsSummary(String token);
 * String getDailyEarnings(String token); String getWeeklyEarnings(String
 * token); String getCashBalance(String token); String getOrderStats(String
 * token); String getOrderHistory(String token); String getRatings(String
 * token); String getWeeklyPerformance(String token); String
 * getRiderProfile(String token); String getWalletBalance(String token); // Add
 * to RiderQueryService.java interface String getRecentOrders(String token); }
 */  

package com.dhatvibs.modules.rider.service;

public interface RiderQueryService {

    String getEarningsSummary(String token);
    String getDailyEarnings(String token);
    String getWeeklyEarnings(String token);
    String getCashBalance(String token);
    String getOrderStats(String token);
    String getOrderHistory(String token);
    String getRecentOrders(String token);  // ← ADD THIS
    String getRatings(String token);
    String getWeeklyPerformance(String token);
    String getRiderProfile(String token);
    String getWalletBalance(String token);
}