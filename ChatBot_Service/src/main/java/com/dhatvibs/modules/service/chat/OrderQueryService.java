package com.dhatvibs.modules.service.chat;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbOrder;
import com.dhatvibs.modules.entities.chat.CbOrderStatusHistory;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.*;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final CbOrderRepository              orderRepo;
    private final CbOrderStatusHistoryRepository historyRepo;
    private final CbUserRepository               userRepo;

    // Get active order for USER
    public String getActiveOrderStatus(String externalUserId) {
        Optional<CbUser> user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, "USER");
        if (user.isEmpty())
            return "User not found.";

        Optional<CbOrder> order = orderRepo
            .findActiveOrderByUserId(user.get().getId());

        if (order.isEmpty())
            return "You have no active orders right now. "
                 + "Would you like to see your order history?";

        return buildOrderStatusReply(order.get());
    }

    // Get active order for VENDOR
    public String getVendorActiveOrder(
            String externalUserId) {
        Optional<CbUser> user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, "VENDOR");
        if (user.isEmpty())
            return "Vendor not found.";

        Optional<CbOrder> order = orderRepo
            .findActiveOrderByVendorId(user.get().getId());

        if (order.isEmpty())
            return "No active orders at the moment.";

        return buildOrderStatusReply(order.get());
    }

    // Get active order for RIDER
    public String getRiderActiveOrder(
            String externalUserId) {
        Optional<CbUser> user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, "RIDER");
        if (user.isEmpty())
            return "Rider not found.";

        Optional<CbOrder> order = orderRepo
            .findActiveOrderByRiderId(user.get().getId());

        if (order.isEmpty())
            return "No active delivery assigned right now.";

        return buildOrderStatusReply(order.get());
    }

    // Order timeline/history
    public String getOrderTimeline(
            String externalUserId, String appId) {
        Optional<CbUser> user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId);
        if (user.isEmpty()) return "User not found.";

        Optional<CbOrder> order =
            appId.equals("VENDOR")
                ? orderRepo.findActiveOrderByVendorId(
                    user.get().getId())
                : appId.equals("RIDER")
                    ? orderRepo.findActiveOrderByRiderId(
                        user.get().getId())
                    : orderRepo.findActiveOrderByUserId(
                        user.get().getId());

        if (order.isEmpty())
            return "No active order found to show timeline.";

        List<CbOrderStatusHistory> history =
            historyRepo
                .findByCbOrderIdOrderByChangedAtAsc(
                    order.get().getId());

        if (history.isEmpty())
            return "No status history available.";

        StringBuilder sb = new StringBuilder();
        sb.append("📦 Order Journey for ")
          .append(order.get().getExternalOrderId())
          .append(":\n");

        history.forEach(h -> sb
            .append("✅ ")
            .append(h.getToStatus())
            .append(" — ")
            .append(h.getChangedAt()
                      .toLocalTime()
                      .toString()
                      .substring(0,5))
            .append("\n"));

        return sb.toString();
    }

    // Order history list
    public String getOrderHistory(
            String externalUserId, String appId) {
        Optional<CbUser> user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId);
        if (user.isEmpty()) return "User not found.";

        List<CbOrder> orders =
            orderRepo.findTop5ByCbUserIdOrderByPlacedAtDesc(
                user.get().getId());

        if (orders.isEmpty())
            return "No past orders found.";

        StringBuilder sb = new StringBuilder(
            "📋 Your last " + orders.size()
            + " orders:\n");

        orders.forEach(o -> sb
            .append("• ")
            .append(o.getExternalOrderId())
            .append(" | ")
            .append(o.getRestaurantName())
            .append(" | ₹")
            .append(o.getTotalAmount())
            .append(" | ")
            .append(o.getOrderStatus())
            .append("\n"));

        return sb.toString();
    }

    // Build readable order status reply
    private String buildOrderStatusReply(CbOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 Order ID: ")
          .append(order.getExternalOrderId())
          .append("\n");
        sb.append("🍽 Restaurant: ")
          .append(order.getRestaurantName())
          .append("\n");
        sb.append("📦 Status: ")
          .append(formatStatus(order.getOrderStatus()))
          .append("\n");
        sb.append("🛍 Items: ")
          .append(order.getItemsSummary())
          .append("\n");
        sb.append("💰 Total: ₹")
          .append(order.getTotalAmount())
          .append("\n");

        if (order.getEstimatedDelivery() != null
                && !order.getOrderStatus()
                         .contains("CANCEL")
                && !order.getOrderStatus()
                         .equals("DELIVERED")) {
            sb.append("⏱ Estimated Delivery: ")
              .append(order.getEstimatedDelivery()
                           .toLocalTime()
                           .toString()
                           .substring(0,5))
              .append("\n");
        }

        if (order.getOrderStatus().contains("CANCEL")) {
            sb.append("❌ Cancel Reason: ")
              .append(order.getCancelReason())
              .append("\n");
        }

        return sb.toString();
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "ORDER_PLACED"      -> "✅ Order Placed";
            case "ACCEPTED"          -> "✅ Accepted by Restaurant";
            case "PREPARING"         -> "👨‍🍳 Being Prepared";
            case "READY_FOR_PICKUP"  -> "📦 Ready for Pickup";
            case "RIDER_ASSIGNED"    -> "🚴 Rider Assigned";
            case "PICKED_UP"         -> "🚴 Picked Up";
            case "OUT_FOR_DELIVERY"  -> "🚚 Out for Delivery";
            case "DELIVERED"         -> "✅ Delivered";
            case "CANCELLED_BY_USER"   -> "❌ Cancelled by You";
            case "CANCELLED_BY_VENDOR" -> "❌ Cancelled by Restaurant";
            case "CANCELLED_BY_SYSTEM" -> "❌ Cancelled by System";
            default -> status;
        };
    }
}
