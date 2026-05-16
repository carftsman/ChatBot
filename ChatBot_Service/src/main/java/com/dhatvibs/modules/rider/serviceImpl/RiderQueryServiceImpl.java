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

    @Override
    public String getOrderStats(String token) {
        JsonNode d = riderApiClient
            .getOrderStats(token);
        log.info("getOrderStats raw: {}", d);
        if (d == null)
            return "Unable to fetch order stats.";

        JsonNode stats = d.path("stats");
        if (stats.isMissingNode())
            return "Unable to fetch order stats.";

        return "Order Summary:\n"
             + "Total Delivered: "
             + stats.path("delivered").asText("0") + "\n"
             + "Cancelled: "
             + stats.path("rejected").asText("0") + "\n"
             + "Total Notified: "
             + stats.path("totalNotified").asText("0");
    }

    @Override
    public String getOrderHistory(String token) {
        // Reuse getRecentOrders
        return getRecentOrders(token);
    }

    // ── ONLY ONE getRecentOrders method ──────────
	/*
	 * @Override public String getRecentOrders(String token) { JsonNode d =
	 * riderApiClient .getDeliveredOrders(token);
	 * 
	 * log.info("getRecentOrders raw response: {}", d);
	 * 
	 * if (d == null) return "Unable to fetch recent orders.";
	 * 
	 * boolean success = d.path("success") .asBoolean(false); if (!success) return
	 * "Unable to fetch recent orders.";
	 * 
	 * JsonNode ordersNode = d.path("orders");
	 * 
	 * if (ordersNode.isMissingNode() || !ordersNode.isArray() || ordersNode.size()
	 * == 0) return "No recent orders found.";
	 * 
	 * StringBuilder sb = new StringBuilder( "Recent Deliveries:\n");
	 * 
	 * int count = 0; for (JsonNode o : ordersNode) { if (count++ >= 5) break;
	 * 
	 * log.info("Order node: {}", o.toString().substring(0,
	 * Math.min(o.toString().length(), 200)));
	 * 
	 * String orderId = !o.path("_id").isMissingNode() ? o.path("_id").asText() :
	 * !o.path("orderId").isMissingNode() ? o.path("orderId").asText() :
	 * !o.path("id").isMissingNode() ? o.path("id").asText() : "N/A";
	 * 
	 * String amount = !o.path("riderEarnings").isMissingNode() ?
	 * o.path("riderEarnings").asText() : !o.path("amount").isMissingNode() ?
	 * o.path("amount").asText() : !o.path("totalAmount").isMissingNode() ?
	 * o.path("totalAmount").asText() : "N/A";
	 * 
	 * String status = !o.path("orderStatus").isMissingNode() ?
	 * o.path("orderStatus").asText() : !o.path("status").isMissingNode() ?
	 * o.path("status").asText() : "DELIVERED";
	 * 
	 * sb.append("• Order: ").append(orderId) .append(" | Rs.").append(amount)
	 * .append(" | ").append(status) .append("\n"); }
	 * 
	 * return sb.toString(); }
	 */
    
	/*
	 * @Override public String getRecentOrders(String token) { JsonNode d =
	 * riderApiClient .getDeliveredOrders(token);
	 * 
	 * // Log FULL response to see exact structure
	 * log.info("getRecentOrders FULL response: {}", d != null ? d.toPrettyString()
	 * : "NULL");
	 * 
	 * if (d == null) return "Unable to fetch recent orders. " +
	 * "Node.js API returned null.";
	 * 
	 * // Log all top level keys log.info("Response keys: {}", d.fieldNames());
	 * 
	 * // Check success field log.info("success field: {}",
	 * d.path("success").asText("MISSING"));
	 * 
	 * // ── Try different response structures ─────────
	 * 
	 * // Structure 1: { success, count, orders: [...] } if (d.has("orders") &&
	 * d.path("orders").isArray()) { return parseOrdersArray( d.path("orders")); }
	 * 
	 * // Structure 2: { success, data: [...] } if (d.has("data") &&
	 * d.path("data").isArray()) { return parseOrdersArray(d.path("data")); }
	 * 
	 * // Structure 3: response itself is array if (d.isArray()) { return
	 * parseOrdersArray(d); }
	 * 
	 * // Structure 4: { success, result: [...] } if (d.has("result") &&
	 * d.path("result").isArray()) { return parseOrdersArray( d.path("result")); }
	 * 
	 * // Structure 5: { success, deliveries: [...] } if (d.has("deliveries") &&
	 * d.path("deliveries").isArray()) { return parseOrdersArray(
	 * d.path("deliveries")); }
	 * 
	 * // Nothing matched — show raw response // so we can see what is coming
	 * log.error("Unknown response structure: {}", d.toPrettyString());
	 * 
	 * return "Received response but could not parse. " + "Keys found: " +
	 * d.fieldNames(); }
	 * 
	 * private String parseOrdersArray(JsonNode array) { if (array == null ||
	 * !array.isArray() || array.size() == 0) return "No recent orders found.";
	 * 
	 * StringBuilder sb = new StringBuilder( "Recent Deliveries:\n");
	 * 
	 * int count = 0; for (JsonNode o : array) { if (count++ >= 5) break;
	 * 
	 * log.info("Order fields: {}", o.fieldNames().toString());
	 * log.info("Order data: {}", o.toPrettyString().substring(0, Math.min(
	 * o.toPrettyString().length(), 300)));
	 * 
	 * // Try all possible orderId fields String orderId = getFirstNonNull(o, "_id",
	 * "orderId", "order_id", "id", "orderNumber");
	 * 
	 * // Try all possible amount fields String amount = getFirstNonNull(o,
	 * "riderEarnings", "amount", "totalAmount", "total", "fare", "earnings");
	 * 
	 * // Try all possible status fields String status = getFirstNonNull(o,
	 * "orderStatus", "status", "state", "deliveryStatus");
	 * 
	 * sb.append("• Order: ").append(orderId) .append(" | Rs.").append(amount)
	 * .append(" | ").append(status) .append("\n"); }
	 * 
	 * return sb.toString(); }
	 * 
	 * private String getFirstNonNull( JsonNode node, String... fields) { for
	 * (String field : fields) { if (!node.path(field).isMissingNode() &&
	 * !node.path(field).isNull() && !node.path(field) .asText().isBlank() &&
	 * !node.path(field) .asText().equals("null")) { return
	 * node.path(field).asText(); } } return "N/A"; }
	 */
    
    
	/*
	 * @Override public String getRecentOrders(String token) { JsonNode d =
	 * riderApiClient .getDeliveredOrders(token);
	 * 
	 * log.info("getRecentOrders response: {}", d != null ? d.toPrettyString()
	 * .substring(0, Math.min( d.toPrettyString() .length(), 500)) : "NULL");
	 * 
	 * if (d == null) return "Unable to fetch recent orders.";
	 * 
	 * // /api/profile/orders/history response: // { success, filter, totalOrders,
	 * // totalRiderEarnings, orders: [...] }
	 * 
	 * // Try "orders" key first if (d.has("orders") && d.path("orders").isArray())
	 * { return buildOrderList(d.path("orders")); }
	 * 
	 * // Try "data" key if (d.has("data") && d.path("data").isArray()) { return
	 * buildOrderList(d.path("data")); }
	 * 
	 * // Try direct array if (d.isArray()) { return buildOrderList(d); }
	 * 
	 * // Show summary stats if no order list if (d.has("totalOrders")) { return
	 * "Total Orders: " + d.path("totalOrders").asText("0") +
	 * "\nTotal Earnings: Rs." + d.path("totalRiderEarnings") .asText("0"); }
	 * 
	 * log.warn("Unknown structure. Keys: {}", d.fieldNames()); return
	 * "No recent orders found."; }
	 * 
	 * private String buildOrderList(JsonNode array) { if (array == null ||
	 * array.size() == 0) return "No recent orders found.";
	 * 
	 * StringBuilder sb = new StringBuilder( "Recent Deliveries:\n");
	 * 
	 * int count = 0; for (JsonNode o : array) { if (count++ >= 5) break;
	 * 
	 * log.info("Order fields available: {}", o.fieldNames());
	 * 
	 * String orderId = getFirst(o, "_id", "orderId", "order_id", "id",
	 * "orderNumber");
	 * 
	 * String amount = getFirst(o, "riderEarnings", "amount", "totalAmount", "fare",
	 * "total");
	 * 
	 * String status = getFirst(o, "orderStatus", "status", "state",
	 * "deliveryStatus");
	 * 
	 * sb.append("• ").append(orderId) .append(" | Rs.").append(amount)
	 * .append(" | ").append(status) .append("\n"); } return sb.toString(); }
	 * 
	 * private String getFirst( JsonNode node, String... fields) { for (String f :
	 * fields) { JsonNode v = node.path(f); if (!v.isMissingNode() && !v.isNull() &&
	 * !v.asText().isBlank() && !v.asText().equals("null")) { return v.asText(); } }
	 * return "N/A"; }
	 */
    
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

    @Override
    public String getRiderProfile(String token) {
        JsonNode d = riderApiClient
            .getRiderProfile(token);
        if (d == null)
            return "Unable to fetch profile.";
        return "Profile:\n"
             + "Name: "   + safe(d, "name") + "\n"
             + "Phone: "  + safe(d, "phone") + "\n"
             + "Status: " + safe(d, "status");
    }

    @Override
    public String getWalletBalance(String token) {
        JsonNode d = riderApiClient.getWallet(token);
        if (d == null)
            return "Unable to fetch wallet.";
        return "Wallet Balance: Rs."
             + safe(d, "balance") + "\n"
             + "Pending: Rs."
             + safe(d, "pending");
    }

    private String safe(JsonNode node, String key) {
        return node.path(key).asText("N/A");
    }
}