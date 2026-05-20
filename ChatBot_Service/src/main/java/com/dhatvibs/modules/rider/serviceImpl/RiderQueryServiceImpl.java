package com.dhatvibs.modules.rider.serviceImpl;

import com.dhatvibs.modules.rider.client.RiderApiClient;
import com.dhatvibs.modules.rider.service.RiderQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderQueryServiceImpl
        implements RiderQueryService {

    private final RiderApiClient riderApiClient;

    @Override
    public String getEarningsSummary(String token) {
        JsonNode d = riderApiClient
            .getEarningsSummary(token);
        if (d == null)
            return "Unable to fetch earnings summary.";
        return "Earnings Summary:\n"
             + "Today:  Rs." + safe(d, "today") + "\n"
             + "Week:   Rs." + safe(d, "week") + "\n"
             + "Month:  Rs." + safe(d, "month");
    }

    @Override
    public String getDailyEarnings(String token) {
        JsonNode d = riderApiClient
            .getDailyEarnings(token);
        if (d == null)
            return "Unable to fetch today's earnings.";
        return "Today's Earnings: Rs."
             + safe(d, "total") + "\n"
             + "Deliveries: "
             + safe(d, "deliveries");
    }

    @Override
    public String getWeeklyEarnings(String token) {
        JsonNode d = riderApiClient
            .getWeeklyEarnings(token);
        if (d == null)
            return "Unable to fetch weekly earnings.";
        return "Weekly Earnings: Rs."
             + safe(d, "total") + "\n"
             + "Deliveries: "
             + safe(d, "deliveries");
    }

    @Override
    public String getCashBalance(String token) {
        JsonNode d = riderApiClient
            .getCashBalance(token);
        if (d == null)
            return "Unable to fetch cash balance.";
        return "COD Cash in Hand: Rs."
             + safe(d, "cashBalance") + "\n"
             + "Pending Handover: Rs."
             + safe(d, "pendingHandover");
    }

	/*
	 * @Override public String getOrderStats(String token) { JsonNode d =
	 * riderApiClient .getOrderStats(token); log.info("getOrderStats raw: {}", d);
	 * if (d == null) return "Unable to fetch order stats.";
	 * 
	 * JsonNode stats = d.path("stats"); if (stats.isMissingNode()) return
	 * "Unable to fetch order stats.";
	 * 
	 * return "Order Summary:\n" + "Total Delivered: " +
	 * stats.path("delivered").asText("0") + "\n" + "Cancelled: " +
	 * stats.path("rejected").asText("0") + "\n" + "Total Notified: " +
	 * stats.path("totalNotified").asText("0"); }
	 */
    
	/*
	 * @Override public String getOrderStats(String token) { JsonNode d =
	 * riderApiClient .getOrderStats(token);
	 * 
	 * log.info("getOrderStats raw: {}", d);
	 * 
	 * if (d == null) return "Unable to fetch order stats.";
	 * 
	 * // Try nested stats object first JsonNode stats = d.path("stats");
	 * 
	 * if (!stats.isMissingNode() && stats.isObject()) { return "Order Summary:\n" +
	 * "Total Delivered: " + getFirst(stats, "delivered", "totalDelivered",
	 * "completedOrders") + "\n" + "Cancelled: " + getFirst(stats, "rejected",
	 * "cancelled", "cancelledOrders") + "\n" + "Total Assigned: " + getFirst(stats,
	 * "totalNotified", "totalAssigned", "totalOrders"); }
	 * 
	 * // Try direct fields return "Order Summary:\n" + "Total Delivered: " +
	 * getFirst(d, "delivered", "totalDelivered", "completedOrders") + "\n" +
	 * "Cancelled: " + getFirst(d, "cancelled", "rejected") + "\n" +
	 * "Total Assigned: " + getFirst(d, "totalNotified", "totalAssigned", "total");
	 * }
	 */
    
    @Override
    public String getOrderDetails(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            return "No order selected. Please tap "
                 + "an order from the list first.";
        }

        JsonNode d = riderApiClient
            .getOrderDetails(orderId);

        log.info("getOrderDetails raw: {}", d);

        if (d == null)
            return "Unable to fetch order details. "
                 + "Please try again.";

        // Response nested in "filteredOrder"
        JsonNode order = d.path("filteredOrder");

        if (order.isMissingNode()
                || order.isNull()) {
            return "Order details not found "
                 + "for order: " + orderId;
        }

        // Build response from filteredOrder fields
        StringBuilder sb = new StringBuilder();
        sb.append("Order Details:\n");

        // Order ID
        sb.append("Order: ")
          .append(order.path("orderId")
                       .asText(orderId))
          .append("\n");

        // Store name
        if (!order.path("vendorShopName")
                  .isMissingNode()) {
            sb.append("Store: ")
              .append(order.path("vendorShopName")
                           .asText())
              .append("\n");
        }

        // Items
        JsonNode items = order.path("items");
        if (!items.isMissingNode()
                && items.isArray()
                && items.size() > 0) {
            sb.append("Items:\n");
            for (JsonNode item : items) {
                sb.append("  • ")
                  .append(item.path("itemName")
                              .asText("Item"))
                  .append(" x")
                  .append(item.path("quantity")
                              .asText("1"))
                  .append(" = Rs.")
                  .append(item.path("total")
                              .asText("0"))
                  .append("\n");
            }
        }

        // Pricing
        JsonNode pricing = order.path("pricing");
        if (!pricing.isMissingNode()) {
            sb.append("Total: Rs.")
              .append(pricing.path("totalAmount")
                             .asText("0"))
              .append("\n");
            if (!pricing.path("riderEarnings")
                        .isMissingNode()) {
                sb.append("Your Earnings: Rs.")
                  .append(pricing.path("riderEarnings")
                                 .asText("0"))
                  .append("\n");
            }
        }

        // Pickup address
        JsonNode pickup = order.path("pickupAddress");
        if (!pickup.isMissingNode()) {
            sb.append("Pickup: ")
              .append(pickup.path("addressLine")
                            .asText("N/A"))
              .append("\n");
        }

        // Delivery address
        JsonNode delivery = order.path("deliveryAddress");
        if (!delivery.isMissingNode()) {
            sb.append("Deliver to: ")
              .append(delivery.path("name")
                              .asText("N/A"))
              .append(", ")
              .append(delivery.path("addressLine")
                              .asText("N/A"))
              .append("\n");
        }

        return sb.toString();
    }
    @Override
    public String getOrderHistory(String token) {
        // Reuse getRecentOrders
        return getRecentOrders(token);
    }

   
    
    @Override
    public String getRecentOrders(String token) {
        JsonNode d = riderApiClient
            .getDeliveredOrders(token);

        if (d == null)
            return "Unable to fetch recent orders.";

        // Response has "data" array (not "orders")
        JsonNode array = d.path("data");

        if (array.isMissingNode()
                || !array.isArray()
                || array.size() == 0) {
            // Fallback to summary stats
            if (d.has("totalOrders")) {
                return "Total Orders: "
                     + d.path("totalOrders").asText("0")
                     + "\nTotal Earnings: Rs."
                     + d.path("totalRiderEarnings")
                        .asText("0");
            }
            return "No recent orders found.";
        }

        return buildOrderList(array);
    }

    private String buildOrderList(JsonNode array) {
        if (array == null || array.size() == 0)
            return "No recent orders found.";

        StringBuilder sb = new StringBuilder(
            "Recent Deliveries:\n");

        int count = 0;
        for (JsonNode o : array) {
            if (count++ >= 5) break;

            // orderId is direct field
            String orderId = o.path("orderId")
                              .asText("N/A");

            // amount is nested inside "pricing" object
            String amount = "N/A";
            JsonNode pricing = o.path("pricing");
            if (!pricing.isMissingNode()) {
                // Use riderEarnings (what rider gets)
                // or totalAmount (order total)
                if (!pricing.path("riderEarnings")
                            .isMissingNode()) {
                    amount = pricing.path("riderEarnings")
                                    .asText();
                } else if (!pricing.path("totalAmount")
                                   .isMissingNode()) {
                    amount = pricing.path("totalAmount")
                                    .asText();
                }
            }

            // status is direct field
            String status = o.path("status").asText(
                o.path("orderStatus").asText("DELIVERED"));

            // Get restaurant name if available
            String restaurant = "";
            if (!o.path("restaurantName").isMissingNode()) {
                restaurant = " | "
                    + o.path("restaurantName").asText();
            } else if (!o.path("storeName")
                         .isMissingNode()) {
                restaurant = " | "
                    + o.path("storeName").asText();
            }

            sb.append("• ").append(orderId)
              .append(restaurant)
              .append(" | Rs.").append(amount)
              .append(" | ").append(status)
              .append("\n");
        }

        return sb.toString();
    }
    
    
    @Override
    public String getRatings(String token) {
        JsonNode d = riderApiClient.getRatings(token);
        if (d == null)
            return "Unable to fetch ratings.";
        return "Your Rating: "
             + safe(d, "average") + " / 5\n"
             + "Total Ratings: "
             + safe(d, "total");
    }

    @Override
    public String getWeeklyPerformance(String token) {
        JsonNode d = riderApiClient
            .getWeeklyPerformance(token);
        if (d == null)
            return "Unable to fetch performance.";
        return "Weekly Performance:\n"
             + "Rating: "
             + safe(d, "rating") + " / 5\n"
             + "Deliveries: "
             + safe(d, "deliveries") + "\n"
             + "Completion Rate: "
             + safe(d, "completionRate") + "%";
    }

	/*
	 * @Override public String getRiderProfile(String token) { JsonNode d =
	 * riderApiClient .getRiderProfile(token); if (d == null) return
	 * "Unable to fetch profile."; return "Profile:\n" + "Name: " + safe(d, "name")
	 * + "\n" + "Phone: " + safe(d, "phone") + "\n" + "Status: " + safe(d,
	 * "status"); }
	 */
    
    @Override
    public String getRiderProfile(String token) {
        JsonNode d = riderApiClient
            .getRiderProfile(token);

        log.info("getRiderProfile raw: {}", d);

        if (d == null)
            return "Unable to fetch profile.";

        // Unwrap nested object
        // Node.js may return { data: {...} }
        // or { rider: {...} } or direct fields
        JsonNode profile = d;
        if (d.has("data")
                && d.path("data").isObject()) {
            profile = d.path("data");
        } else if (d.has("rider")
                && d.path("rider").isObject()) {
            profile = d.path("rider");
        } else if (d.has("riderProfile")
                && d.path("riderProfile").isObject()) {
            profile = d.path("riderProfile");
        }

        return "Profile:\n"
             + "Name: "
             + getFirst(profile,
                 "name", "fullName",
                 "riderName", "firstName") + "\n"
             + "Phone: "
             + getFirst(profile,
                 "phone", "phoneNumber",
                 "mobile", "contactNumber") + "\n"
             + "Status: "
             + getFirst(profile,
                 "status", "accountStatus",
                 "riderStatus", "isActive");
    }

	/*
	 * @Override public String getWalletBalance(String token) { JsonNode d =
	 * riderApiClient.getWallet(token); if (d == null) return
	 * "Unable to fetch wallet."; return "Wallet Balance: Rs." + safe(d, "balance")
	 * + "\n" + "Pending: Rs." + safe(d, "pending"); }
	 */
    
    @Override
    public String getWalletBalance(String token) {
        JsonNode d = riderApiClient.getWallet(token);

        log.info("getWallet raw: {}", d);

        if (d == null)
            return "Unable to fetch wallet.";

        // Unwrap nested object
        JsonNode wallet = d;
        if (d.has("data")
                && d.path("data").isObject()) {
            wallet = d.path("data");
        } else if (d.has("wallet")
                && d.path("wallet").isObject()) {
            wallet = d.path("wallet");
        }

        return "Wallet Balance: Rs."
             + getFirst(wallet,
                 "balance", "walletBalance",
                 "amount", "currentBalance",
                 "availableBalance") + "\n"
             + "Pending: Rs."
             + getFirst(wallet,
                 "pending", "pendingAmount",
                 "pendingBalance", "holdAmount");
    }
    
    private String getFirst(
            JsonNode node, String... fields) {
        for (String f : fields) {
            JsonNode v = node.path(f);
            if (!v.isMissingNode()
                    && !v.isNull()
                    && !v.asText().isBlank()
                    && !v.asText().equals("null")) {
                return v.asText();
            }
        }
        return "N/A";
    }

   

    private String safe(JsonNode node, String key) {
        return node.path(key).asText("N/A");
    }
}