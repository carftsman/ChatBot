package com.dhatvibs.modules.consumer.service;

import java.util.List;
import java.util.Map;



public interface ConsumerQueryService {
	/*
	 * String getRecentOrders(String token, String consumerId);
	 */
	
	List<Map> getRecentOrders(String token,
            String consumerId);
    String getOrderDetails(String orderId,
                           String token);
    String cancelOrder(String orderId, String token);
    String getOrderNotDelivered(String orderId,
                                String token);
    String getRefundStatus(String orderId,
                           String token);
    String getPaymentStatus(String orderId,
                            String token);
    String getConsumerProfile(String token,
                              String consumerId);
    String getAddresses(String token);
}