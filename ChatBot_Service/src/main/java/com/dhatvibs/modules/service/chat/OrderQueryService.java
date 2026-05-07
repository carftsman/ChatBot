package com.dhatvibs.modules.service.chat;

/*
 * import lombok.RequiredArgsConstructor; import
 * org.springframework.stereotype.Service;
 * 
 * import com.dhatvibs.modules.entities.auth.CbUser; import
 * com.dhatvibs.modules.entities.chat.CbOrder; import
 * com.dhatvibs.modules.entities.chat.CbOrderStatusHistory; import
 * com.dhatvibs.modules.repository.auth.CbUserRepository; import
 * com.dhatvibs.modules.repository.chat.*;
 * 
 * import java.time.LocalDateTime; import java.util.*;
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class OrderQueryService {
 * 
 * private final CbOrderRepository orderRepo; private final
 * CbOrderStatusHistoryRepository historyRepo; private final CbUserRepository
 * userRepo;
 * 
 * // Get active order for USER public String getActiveOrderStatus(String
 * externalUserId) { Optional<CbUser> user = userRepo
 * .findByExternalUserIdAndAppId( externalUserId, "USER"); if (user.isEmpty())
 * return "User not found.";
 * 
 * Optional<CbOrder> order = orderRepo
 * .findActiveOrderByUserId(user.get().getId());
 * 
 * if (order.isEmpty()) return "You have no active orders right now. " +
 * "Would you like to see your order history?";
 * 
 * return buildOrderStatusReply(order.get()); }
 * 
 * // Get active order for VENDOR public String getVendorActiveOrder( String
 * externalUserId) { Optional<CbUser> user = userRepo
 * .findByExternalUserIdAndAppId( externalUserId, "VENDOR"); if (user.isEmpty())
 * return "Vendor not found.";
 * 
 * Optional<CbOrder> order = orderRepo
 * .findActiveOrderByVendorId(user.get().getId());
 * 
 * if (order.isEmpty()) return "No active orders at the moment.";
 * 
 * return buildOrderStatusReply(order.get()); }
 * 
 * // Get active order for RIDER public String getRiderActiveOrder( String
 * externalUserId) { Optional<CbUser> user = userRepo
 * .findByExternalUserIdAndAppId( externalUserId, "RIDER"); if (user.isEmpty())
 * return "Rider not found.";
 * 
 * Optional<CbOrder> order = orderRepo
 * .findActiveOrderByRiderId(user.get().getId());
 * 
 * if (order.isEmpty()) return "No active delivery assigned right now.";
 * 
 * return buildOrderStatusReply(order.get()); }
 * 
 * // Order timeline/history public String getOrderTimeline( String
 * externalUserId, String appId) { Optional<CbUser> user = userRepo
 * .findByExternalUserIdAndAppId( externalUserId, appId); if (user.isEmpty())
 * return "User not found.";
 * 
 * Optional<CbOrder> order = appId.equals("VENDOR") ?
 * orderRepo.findActiveOrderByVendorId( user.get().getId()) :
 * appId.equals("RIDER") ? orderRepo.findActiveOrderByRiderId(
 * user.get().getId()) : orderRepo.findActiveOrderByUserId( user.get().getId());
 * 
 * if (order.isEmpty()) return "No active order found to show timeline.";
 * 
 * List<CbOrderStatusHistory> history = historyRepo
 * .findByCbOrderIdOrderByChangedAtAsc( order.get().getId());
 * 
 * if (history.isEmpty()) return "No status history available.";
 * 
 * StringBuilder sb = new StringBuilder(); sb.append("📦 Order Journey for ")
 * .append(order.get().getExternalOrderId()) .append(":\n");
 * 
 * history.forEach(h -> sb .append("✅ ") .append(h.getToStatus()) .append(" — ")
 * .append(h.getChangedAt() .toLocalTime() .toString() .substring(0,5))
 * .append("\n"));
 * 
 * return sb.toString(); }
 * 
 * // Order history list public String getOrderHistory( String externalUserId,
 * String appId) { Optional<CbUser> user = userRepo
 * .findByExternalUserIdAndAppId( externalUserId, appId); if (user.isEmpty())
 * return "User not found.";
 * 
 * List<CbOrder> orders = orderRepo.findTop5ByCbUserIdOrderByPlacedAtDesc(
 * user.get().getId());
 * 
 * if (orders.isEmpty()) return "No past orders found.";
 * 
 * StringBuilder sb = new StringBuilder( "📋 Your last " + orders.size() +
 * " orders:\n");
 * 
 * orders.forEach(o -> sb .append("• ") .append(o.getExternalOrderId())
 * .append(" | ") .append(o.getRestaurantName()) .append(" | ₹")
 * .append(o.getTotalAmount()) .append(" | ") .append(o.getOrderStatus())
 * .append("\n"));
 * 
 * return sb.toString(); }
 * 
 * // Build readable order status reply private String
 * buildOrderStatusReply(CbOrder order) { StringBuilder sb = new
 * StringBuilder(); sb.append("🛒 Order ID: ")
 * .append(order.getExternalOrderId()) .append("\n");
 * sb.append("🍽 Restaurant: ") .append(order.getRestaurantName())
 * .append("\n"); sb.append("📦 Status: ")
 * .append(formatStatus(order.getOrderStatus())) .append("\n");
 * sb.append("🛍 Items: ") .append(order.getItemsSummary()) .append("\n");
 * sb.append("💰 Total: ₹") .append(order.getTotalAmount()) .append("\n");
 * 
 * if (order.getEstimatedDelivery() != null && !order.getOrderStatus()
 * .contains("CANCEL") && !order.getOrderStatus() .equals("DELIVERED")) {
 * sb.append("⏱ Estimated Delivery: ") .append(order.getEstimatedDelivery()
 * .toLocalTime() .toString() .substring(0,5)) .append("\n"); }
 * 
 * if (order.getOrderStatus().contains("CANCEL")) {
 * sb.append("❌ Cancel Reason: ") .append(order.getCancelReason())
 * .append("\n"); }
 * 
 * return sb.toString(); }
 * 
 * private String formatStatus(String status) { return switch (status) { case
 * "ORDER_PLACED" -> "✅ Order Placed"; case "ACCEPTED" ->
 * "✅ Accepted by Restaurant"; case "PREPARING" -> "👨‍🍳 Being Prepared"; case
 * "READY_FOR_PICKUP" -> "📦 Ready for Pickup"; case "RIDER_ASSIGNED" ->
 * "🚴 Rider Assigned"; case "PICKED_UP" -> "🚴 Picked Up"; case
 * "OUT_FOR_DELIVERY" -> "🚚 Out for Delivery"; case "DELIVERED" ->
 * "✅ Delivered"; case "CANCELLED_BY_USER" -> "❌ Cancelled by You"; case
 * "CANCELLED_BY_VENDOR" -> "❌ Cancelled by Restaurant"; case
 * "CANCELLED_BY_SYSTEM" -> "❌ Cancelled by System"; default -> status; }; } }
 */  



import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.*;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.CbOrderRepository;
import com.dhatvibs.modules.repository.chat.CbOrderStatusHistoryRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    private final CbOrderRepository              orderRepo;
    private final CbOrderStatusHistoryRepository historyRepo;
    private final CbUserRepository               userRepo;

    // ─────────────────────────────────────────────
    // Get order status
    // Uses contextOrderId if available
    // Falls back to latest order by userId
    // ─────────────────────────────────────────────
    public String getOrderStatus(
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        Optional<CbOrder> order;

        if (contextOrderId != null) {
            // Use order from session context directly
            order = orderRepo.findById(contextOrderId);
        } else {
            // Fallback — find latest order by userId
            CbUser user = findUser(externalUserId, appId);
            if (user == null)
                return "User not found.";
            order = getActiveOrder(user, appId);
        }

        return order.map(this::buildOrderStatusReply)
            .orElse("No active order found.");
    }

    // ─────────────────────────────────────────────
    // Get order timeline
    // ─────────────────────────────────────────────
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
            historyRepo
                .findByCbOrderIdOrderByChangedAtAsc(
                    order.get().getId());

        if (history.isEmpty())
            return "No status history available yet.";

        StringBuilder sb = new StringBuilder();
        sb.append("📦 Order Journey:\n");
        history.forEach(h -> sb
            .append("✅ ")
            .append(formatStatus(h.getToStatus()))
            .append(" — ")
            .append(h.getChangedAt()
                      .toLocalTime()
                      .toString()
                      .substring(0, 5))
            .append("\n"));

        return sb.toString();
    }

    // ─────────────────────────────────────────────
    // Check cancel eligibility
    // ─────────────────────────────────────────────
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
            return "✅ Your order can be cancelled.\n"
                 + "Please go to Orders section and "
                 + "tap Cancel Order.";
        } else if (status.equals("ACCEPTED")) {
            return "⚠️ Your order is already accepted "
                 + "by the restaurant.\n"
                 + "Cancellation may not be possible. "
                 + "A support ticket will be raised.";
        } else {
            return "❌ Your order cannot be cancelled "
                 + "at this stage.\n"
                 + "Status: " + formatStatus(status);
        }
    }

    // ─────────────────────────────────────────────
    // Get order history (last 5)
    // ─────────────────────────────────────────────
    public String getOrderHistory(
            String externalUserId, String appId) {

        CbUser user = findUser(externalUserId, appId);
        if (user == null) return "User not found.";

        List<CbOrder> orders =
            orderRepo
                .findTop5ByCbUserIdOrderByPlacedAtDesc(
                    user.getId());

        if (orders.isEmpty())
            return "No past orders found.";

        StringBuilder sb = new StringBuilder(
            "📋 Your recent orders:\n");
        orders.forEach(o -> sb
            .append("• ")
            .append(o.getExternalOrderId())
            .append(" | ")
            .append(o.getRestaurantName())
            .append(" | ₹")
            .append(o.getTotalAmount())
            .append(" | ")
            .append(formatStatus(o.getOrderStatus()))
            .append("\n"));

        return sb.toString();
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────
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
            default ->
                Optional.empty();
        };
    }

    private String buildOrderStatusReply(CbOrder o) {
        StringBuilder sb = new StringBuilder();
        sb.append("🛒 Order: ")
          .append(o.getExternalOrderId()).append("\n");
        sb.append("🍽 Restaurant: ")
          .append(o.getRestaurantName()).append("\n");
        sb.append("📦 Status: ")
          .append(formatStatus(o.getOrderStatus()))
          .append("\n");
        sb.append("🛍 Items: ")
          .append(o.getItemsSummary()).append("\n");
        sb.append("💰 Total: ₹")
          .append(o.getTotalAmount()).append("\n");

        if (o.getEstimatedDelivery() != null
                && !o.getOrderStatus().contains("CANCEL")
                && !o.getOrderStatus()
                     .equals("DELIVERED")) {
            sb.append("⏱ ETA: ")
              .append(o.getEstimatedDelivery()
                        .toLocalTime()
                        .toString()
                        .substring(0, 5))
              .append("\n");
        }

        if (o.getOrderStatus().contains("CANCEL")) {
            sb.append("❌ Reason: ")
              .append(o.getCancelReason()).append("\n");
        }
        return sb.toString();
    }

    private String formatStatus(String status) {
        return switch (status) {
            case "ORDER_PLACED"        -> "✅ Order Placed";
            case "ACCEPTED"            -> "✅ Accepted";
            case "PREPARING"           -> "👨‍🍳 Preparing";
            case "READY_FOR_PICKUP"    -> "📦 Ready for Pickup";
            case "RIDER_ASSIGNED"      -> "🚴 Rider Assigned";
            case "PICKED_UP"           -> "🚴 Picked Up";
            case "OUT_FOR_DELIVERY"    -> "🚚 Out for Delivery";
            case "DELIVERED"           -> "✅ Delivered";
            case "CANCELLED_BY_USER"   -> "❌ Cancelled by You";
            case "CANCELLED_BY_VENDOR" -> "❌ Cancelled by Restaurant";
            case "CANCELLED_BY_SYSTEM" -> "❌ Cancelled by System";
            default -> status;
        };
    }
}