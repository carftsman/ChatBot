package com.dhatvibs.modules.serviceImpl.chat;


import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.*;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.*;
import com.dhatvibs.modules.service.chat.OrderQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderQueryServiceImpl
        implements OrderQueryService {

    private final CbOrderRepository              orderRepo;
    private final CbOrderStatusHistoryRepository historyRepo;
    private final CbUserRepository               userRepo;

    @Override
    public String getOrderStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        Optional<CbOrder> order;
        if (contextOrderId != null) {
            order = orderRepo.findById(contextOrderId);
        } else {
            CbUser user = findUser(externalUserId, appId);
            if (user == null) return "User not found.";
            order = getActiveOrder(user, appId);
        }
        return order.map(this::buildOrderStatusReply)
            .orElse("No active order found.");
    }

    @Override
    public String getOrderTimeline(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        Optional<CbOrder> order;
        if (contextOrderId != null) {
            order = orderRepo.findById(contextOrderId);
        } else {
            CbUser user = findUser(externalUserId, appId);
            if (user == null) return "User not found.";
            order = getActiveOrder(user, appId);
        }

        if (order.isEmpty())
            return "No order found to show timeline.";

        List<CbOrderStatusHistory> history =
            historyRepo.findByCbOrderIdOrderByChangedAtAsc(
                order.get().getId());

        if (history.isEmpty())
            return "No status history available yet.";

        StringBuilder sb = new StringBuilder();
        sb.append("Order Journey:\n");
        history.forEach(h -> sb
            .append("- ")
            .append(h.getToStatus())
            .append(" at ")
            .append(h.getChangedAt()
                      .toLocalTime()
                      .toString()
                      .substring(0, 5))
            .append("\n"));
        return sb.toString();
    }

    @Override
    public String checkCancelEligibility(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        Optional<CbOrder> order;
        if (contextOrderId != null) {
            order = orderRepo.findById(contextOrderId);
        } else {
            CbUser user = findUser(externalUserId, appId);
            if (user == null) return "User not found.";
            order = getActiveOrder(user, appId);
        }

        if (order.isEmpty())
            return "No active order found to cancel.";

        String status = order.get().getOrderStatus();
        if (status.equals("ORDER_PLACED")) {
            return "Your order can be cancelled. "
                 + "Go to Orders and tap Cancel.";
        } else if (status.equals("ACCEPTED")) {
            return "Your order is accepted by restaurant. "
                 + "Cancellation may not be possible. "
                 + "Support ticket will be raised.";
        } else {
            return "Your order cannot be cancelled. "
                 + "Status: " + status;
        }
    }

    @Override
    public String getOrderHistory(
            String externalUserId,
            String appId) {

        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbOrder> orders =
            orderRepo.findTop5ByCbUserIdOrderByPlacedAtDesc(
                user.getId());

        if (orders.isEmpty())
            return "No past orders found.";

        StringBuilder sb = new StringBuilder(
            "Your recent orders:\n");
        orders.forEach(o -> sb
            .append("- ")
            .append(o.getExternalOrderId())
            .append(" | ")
            .append(o.getRestaurantName())
            .append(" | Rs.")
            .append(o.getTotalAmount())
            .append(" | ")
            .append(o.getOrderStatus())
            .append("\n"));
        return sb.toString();
    }

    private CbUser findUser(
            String externalUserId, String appId) {
        return userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElse(null);
    }

    private Optional<CbOrder> getActiveOrder(
            CbUser user, String appId) {
        return switch (appId) {
            case "USER"   ->
                orderRepo.findActiveOrderByUserId(
                    user.getId());
            case "VENDOR" ->
                orderRepo.findActiveOrderByVendorId(
                    user.getId());
            case "RIDER"  ->
                orderRepo.findActiveOrderByRiderId(
                    user.getId());
            default -> Optional.empty();
        };
    }

    private String buildOrderStatusReply(CbOrder o) {
        return "Order: " + o.getExternalOrderId() + "\n"
             + "Restaurant: " + o.getRestaurantName() + "\n"
             + "Status: " + o.getOrderStatus() + "\n"
             + "Items: " + o.getItemsSummary() + "\n"
             + "Total: Rs." + o.getTotalAmount() + "\n";
    }
}
