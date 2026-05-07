package com.dhatvibs.modules.serviceImpl.chat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dhatvibs.modules.dto.chat.RecentOrderResponse;
import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbOrder;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.CbOrderRepository;
import com.dhatvibs.modules.service.chat.OrderService;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final CbOrderRepository orderRepo;
    private final CbUserRepository userRepo;

    @Override
    public List<RecentOrderResponse> getRecentOrders(
            String externalUserId, String appId) {

        // Find user in cb_users
        CbUser user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"));

        List<CbOrder> orders;

        // Fetch orders based on role
        switch (appId) {
            case "USER" ->
                orders = orderRepo
                    .findTop5ByCbUserIdOrderByPlacedAtDesc(
                        user.getId());
            case "VENDOR" ->
                orders = orderRepo
                    .findTop5ByCbVendorIdOrderByPlacedAtDesc(
                        user.getId());
            case "RIDER" ->
                orders = orderRepo
                    .findTop5ByCbRiderIdOrderByPlacedAtDesc(
                        user.getId());
            default ->
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid appId: " + appId);
        }

        if (orders.isEmpty()) {
            log.info("No orders found for userId: {}",
                     externalUserId);
        }

        // Map to response DTO
        return orders.stream()
            .map(o -> RecentOrderResponse.builder()
                .orderId(o.getId())
                .externalOrderId(o.getExternalOrderId())
                .restaurantName(o.getRestaurantName())
                .orderStatus(o.getOrderStatus())
                .totalAmount(o.getTotalAmount())
                .itemsSummary(o.getItemsSummary())
                .placedAt(o.getPlacedAt())
                .build())
            .collect(Collectors.toList());
    }
}
