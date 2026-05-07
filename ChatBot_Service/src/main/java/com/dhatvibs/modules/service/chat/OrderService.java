package com.dhatvibs.modules.service.chat;


import java.util.List;

import com.dhatvibs.modules.dto.chat.RecentOrderResponse;

public interface OrderService {
    List<RecentOrderResponse> getRecentOrders(
            String externalUserId, String appId);
}
