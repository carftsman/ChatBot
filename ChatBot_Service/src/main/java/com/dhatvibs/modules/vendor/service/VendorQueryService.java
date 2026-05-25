package com.dhatvibs.modules.vendor.service;

import java.util.*;

import com.fasterxml.jackson.databind.JsonNode;

public interface VendorQueryService {
	/*
	 * String getPendingOrders(String token); String getDeliveredOrders(String
	 * token); String getCancelledOrders(String token);
	 */
	
	// ← changed from String to List<JsonNode>
	/*
	 * List<JsonNode> getPendingOrders(String token); List<JsonNode>
	 * getDeliveredOrders(String token); List<JsonNode> getCancelledOrders(String
	 * token);
	 */
	
	List<Map> getPendingOrders(String token);
    List<Map> getDeliveredOrders(String token);
    List<Map> getCancelledOrders(String token);
    String getOrderDetails(String orderId,
                           String token);
    String getStoreDetails(String token);
    String getWalletBalance(String token);
    String getTransactionHistory(String token);
    String getPayoutStatus(String token);
}