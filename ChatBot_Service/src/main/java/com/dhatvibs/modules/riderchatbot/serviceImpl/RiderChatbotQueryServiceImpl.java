package com.dhatvibs.modules.riderchatbot.serviceImpl;

import com.dhatvibs.modules.riderchatbot.client
        .RiderChatbotApiClient;
import com.dhatvibs.modules.riderchatbot.service
        .RiderChatbotQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.Iterator;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderChatbotQueryServiceImpl
        implements RiderChatbotQueryService {

    private final RiderChatbotApiClient apiClient;

    // ── EARNINGS ──────────────────────────────────

    @Override
    public String getEarningsSummary(String token) {
        JsonNode d = apiClient
            .getEarningsSummary(token);
        log.info("getEarningsSummary: {}", d);
        if (d == null)
            return "Unable to fetch earnings.";

        return "Earnings Summary:\n"
             + "Today:   Rs."
             + getAt(d, "today/totalEarnings",
                       "today") + "\n"
             + "Week:    Rs."
             + getAt(d, "week/totalEarnings",
                       "week") + "\n"
             + "Month:   Rs."
             + getAt(d, "month/totalEarnings",
                       "month");
    }

    @Override
    public String getDailyEarnings(String token) {
        JsonNode d = apiClient
            .getDailyEarnings(token);
        log.info("getDailyEarnings: {}", d);
        if (d == null)
            return "Unable to fetch daily earnings.";

        return "Today's Earnings:\n"
             + "Total: Rs."
             + get(d, "totalEarnings", "total") + "\n"
             + "Date: "
             + get(d, "date");
    }

    @Override
    public String getWeeklyEarnings(String token) {
        JsonNode d = apiClient
            .getWeeklyEarnings(token);
        log.info("getWeeklyEarnings: {}", d);
        if (d == null)
            return "Unable to fetch weekly earnings.";

        return "Weekly Earnings:\n"
             + "Total: Rs."
             + get(d, "total") + "\n"
             + "Week Range: "
             + get(d, "weekRange") + "\n"
             + "Change: "
             + get(d, "changePercent") + "%";
    }

    @Override
    public String getCashBalance(String token) {
        JsonNode d = apiClient.getCashBalance(token);
        log.info("getCashBalance: {}", d);
        if (d == null)
            return "Unable to fetch cash balance.";

        JsonNode data = d.has("data")
            ? d.path("data") : d;

        return "COD Cash Balance:\n"
             + "Cash in Hand: Rs."
             + get(data, "cashInHand",
                 "totalCash", "amount") + "\n"
             + "Pending Handover: Rs."
             + get(data, "pendingHandover",
                 "pending");
    }

    // ── ORDERS ────────────────────────────────────

	
    
	/*
	 * @Override public String getOrderHistory(String token) { JsonNode d =
	 * apiClient.getOrderHistory(token);
	 * 
	 * log.info("getOrderHistory FULL response: {}", d != null ? d.toPrettyString()
	 * .substring(0, Math.min(500, d.toPrettyString().length())) : "NULL");
	 * 
	 * if (d == null) return "Unable to fetch order history. " +
	 * "Please try again.";
	 * 
	 * // Response: { success, filter, totalOrders, // totalRiderEarnings,
	 * totalDistance, // avgRating, data: [...] }
	 * 
	 * return "Order History:\n" + "Total Orders: " + get(d, "totalOrders") + "\n" +
	 * "Total Earnings: Rs." + get(d, "totalRiderEarnings") + "\n" +
	 * "Total Distance: " + get(d, "totalDistance") + " km\n" + "Avg Rating: " +
	 * get(d, "avgRating") + "/5"; }
	 */
    
	/*
	 * @Override public String getOrderHistory(String token) { JsonNode d =
	 * apiClient.getOrderHistory(token);
	 * 
	 * log.info("getOrderHistory FULL: {}", d != null ? d.toPrettyString()
	 * .substring(0, Math.min(500, d.toPrettyString().length())) : "NULL");
	 * 
	 * if (d == null) return "Unable to fetch orders.";
	 * 
	 * // Response: { success, count, orders: [...] } JsonNode array = null;
	 * 
	 * if (d.has("orders") && d.path("orders").isArray()) { array =
	 * d.path("orders"); } else if (d.has("data") && d.path("data").isArray()) {
	 * array = d.path("data"); } else if (d.isArray()) { array = d; }
	 * 
	 * if (array == null || array.size() == 0) return "No recent orders found.";
	 * 
	 * StringBuilder sb = new StringBuilder( "Recent Deliveries:\n"); int count = 0;
	 * for (JsonNode o : array) { if (count++ >= 5) break;
	 * 
	 * log.info("Order node: {}", o.toString().substring(0, Math.min(200,
	 * o.toString().length())));
	 * 
	 * String orderId = get(o, "orderId", "_id", "id", "orderNumber"); String amount
	 * = get(o, "riderEarnings", "amount", "totalAmount", "fare"); String status =
	 * get(o, "status", "orderStatus");
	 * 
	 * sb.append("• ").append(orderId) .append(" | Rs.").append(amount)
	 * .append(" | ").append(status) .append("\n"); } return sb.toString(); }
	 * 
	 * @Override public String getOrderStats(String token) { JsonNode d =
	 * apiClient.getOrderStats(token); log.info("getOrderStats: {}", d); if (d ==
	 * null) return "Unable to fetch order stats.";
	 * 
	 * JsonNode stats = d.path("stats"); JsonNode orders = d.path("orders");
	 * 
	 * StringBuilder sb = new StringBuilder( "Order Statistics:\n");
	 * 
	 * if (!stats.isMissingNode()) { sb.append("Total: ") .append(get(stats,
	 * "total")).append("\n") .append("Delivered: ") .append(get(stats,
	 * "delivered")) .append("\n") .append("Cancelled: ") .append(get(stats,
	 * "cancelled")) .append("\n") .append("Acceptance Rate: ") .append(get(stats,
	 * "acceptanceRate")) .append("%\n"); }
	 * 
	 * if (!orders.isMissingNode() && orders.isArray()) {
	 * sb.append("Recent Orders: ") .append(orders.size()); }
	 * 
	 * return sb.toString(); }
	 * 
	 * @Override public String getOrderDetails( String orderId, String token) { if
	 * (orderId == null || orderId.isBlank()) return "No order selected. " +
	 * "Please tap an order first.";
	 * 
	 * JsonNode d = apiClient .getOrderDetails(orderId, token);
	 * log.info("getOrderDetails: {}", d); if (d == null) return
	 * "Unable to fetch order details.";
	 * 
	 * JsonNode order = d.has("filteredOrder") ? d.path("filteredOrder") : d;
	 * 
	 * return "Order Details:\n" + "Order: " + get(order, "orderId", "orderNumber")
	 * + "\n" + "Status: " + get(order, "status", "orderStatus") + "\n" + "Store: "
	 * + get(order, "vendorShopName", "storeName") + "\n" + "Amount: Rs." +
	 * get(order, "riderEarnings", "totalAmount"); }
	 */
    
 // ── ORDERS ────────────────────────────────────

	/*
	 * @Override public String getOrderHistory(String token) { //
	 * /api/orders/delivered // Response: { success, count, orders: [...] } JsonNode
	 * d = apiClient.getOrderHistory(token); log.info("getOrderHistory: {}", d);
	 * 
	 * if (d == null) return "Unable to fetch orders.";
	 * 
	 * JsonNode orders = d.path("orders"); if (orders.isMissingNode() ||
	 * !orders.isArray() || orders.size() == 0) return "No recent orders found.";
	 * 
	 * StringBuilder sb = new StringBuilder( "Recent Deliveries:\n"); int count = 0;
	 * for (JsonNode o : orders) { if (count++ >= 5) break; sb.append("• ")
	 * .append(get(o, "orderId", "_id", "id")) .append(" | Rs.") .append(get(o,
	 * "riderEarnings", "amount", "totalAmount")) .append(" | ") .append(get(o,
	 * "status", "orderStatus")) .append("\n"); } return sb.toString(); }
	 */
    
    @Override
    public String getOrderHistory(String token) {
        JsonNode d = apiClient.getOrderHistory(token);
        log.info("getOrderHistory: {}", d);

        if (d == null)
            return "Unable to fetch order history.";

        // Build summary from top level fields
        StringBuilder sb = new StringBuilder(
            "Order History Summary:\n");
        sb.append("Total Orders: ")
          .append(get(d, "totalOrders")).append("\n")
          .append("Total Earnings: Rs.")
          .append(get(d, "totalRiderEarnings")).append("\n")
          .append("Total Distance: ")
          .append(get(d, "totalDistance")).append(" km\n")
          .append("Avg Rating: ")
          .append(get(d, "avgRating")).append("/5\n\n");

        // Show last 3 orders from data array
        JsonNode orders = d.path("data");
        if (!orders.isMissingNode()
                && orders.isArray()
                && orders.size() > 0) {
            sb.append("Recent Deliveries:\n");
            int count = 0;
            for (JsonNode o : orders) {
                if (count++ >= 3) break;

                // riderEarning is inside pricing object
                String earning = "N/A";
                JsonNode pricing = o.path("pricing");
                if (!pricing.isMissingNode()) {
                    earning = get(pricing,
                        "riderEarning",
                        "totalAmount");
                }

                sb.append("• ")
                  .append(get(o, "orderId"))
                  .append(" | Rs.").append(earning)
                  .append(" | ")
                  .append(get(o, "pickupAddress"))
                  .append(" → ")
                  .append(get(o, "deliveredAddress"))
                  .append("\n");
            }
        }

        return sb.toString();
    }

    @Override
    public String getOrderStats(String token) {
        // /api/orders/stats
        // Response: { success, stats: {...}, orders: [...] }
        JsonNode d = apiClient.getOrderStats(token);
        log.info("getOrderStats: {}", d);

        if (d == null)
            return "Unable to fetch order stats.";

        JsonNode stats = d.path("stats");
        if (stats.isMissingNode())
            return "Unable to fetch order stats.";

        return "Order Statistics:\n"
             + "Total: "
             + get(stats, "total",
                 "totalOrders") + "\n"
             + "Delivered: "
             + get(stats, "delivered",
                 "totalDelivered") + "\n"
             + "Cancelled: "
             + get(stats, "cancelled",
                 "totalCancelled") + "\n"
             + "Acceptance Rate: "
             + get(stats, "acceptanceRate") + "%";
    }

    @Override
    public String getOrderDetails(
            String orderId, String token) {
        if (orderId == null || orderId.isBlank())
            return "No order selected. "
                 + "Please tap an order first.";

        // /api/orders/{orderId}/details
        // Response: { success, message, filteredOrder: {...} }
        JsonNode d = apiClient.getOrderDetails(
            orderId, token);
        log.info("getOrderDetails: {}", d);

        if (d == null)
            return "Unable to fetch order details.";

        // filteredOrder is the correct key
        JsonNode order = d.path("filteredOrder");
        if (order.isMissingNode() || order.isNull())
            return "Order details not found.";

        // pricing is nested in filteredOrder
        JsonNode pricing = order.path("pricing");
        String earnings = pricing.isMissingNode()
            ? "N/A"
            : get(pricing, "riderEarnings",
                "totalAmount");

        return "Order Details:\n"
             + "Order: "
             + get(order, "orderId",
                 "orderNumber") + "\n"
             + "Store: "
             + get(order, "vendorShopName",
                 "storeName") + "\n"
             + "Status: "
             + get(order, "status",
                 "orderStatus") + "\n"
             + "Earnings: Rs." + earnings + "\n"
             + "Pickup: "
             + order.at("/pickupAddress/addressLine")
                    .asText("N/A") + "\n"
             + "Deliver to: "
             + order.at("/deliveryAddress/addressLine")
                    .asText("N/A");
    }

    // ── RATINGS ───────────────────────────────────

    @Override
    public String getRatings(String token) {
        JsonNode d = apiClient.getRatings(token);
        log.info("getRatings: {}", d);
        if (d == null)
            return "Unable to fetch ratings.";

        return "Your Ratings:\n"
             + "Average: "
             + get(d, "averageRating",
                 "average") + "/5\n"
             + "Total Ratings: "
             + get(d, "totalRatings", "total");
    }

    @Override
    public String getWeeklyPerformance(
            String token) {
        JsonNode d = apiClient
            .getWeeklyPerformance(token);
        log.info("getWeeklyPerformance: {}", d);
        if (d == null)
            return "Unable to fetch performance.";

        JsonNode data = d.has("data")
            ? d.path("data") : d;

        return "Weekly Performance (Last 7 days):\n"
             + "Rating: "
             + get(data, "averageRating",
                 "rating") + "/5\n"
             + "Deliveries: "
             + get(data, "totalDeliveries",
                 "deliveries") + "\n"
             + "Completion Rate: "
             + get(data, "completionRate") + "%";
    }

    // ── SLOTS ─────────────────────────────────────

    @Override
    public String getActiveSlots(String token) {
        JsonNode d = apiClient.getActiveSlots(token);
        log.info("getActiveSlots: {}", d);
        if (d == null)
            return "Unable to fetch slot info.";

        StringBuilder sb = new StringBuilder(
            "Active Slots:\n");

        JsonNode current = d.path("currentSlot");
        if (!current.isMissingNode()
                && !current.isNull()) {
            sb.append("Current Slot:\n")
              .append("  Time: ")
              .append(get(current, "startTime"))
              .append(" - ")
              .append(get(current, "endTime"))
              .append("\n")
              .append("  Status: ")
              .append(get(current, "status"))
              .append("\n");
        }

        JsonNode next = d.path("nextSlot");
        if (!next.isMissingNode()
                && !next.isNull()) {
            sb.append("Next Slot:\n")
              .append("  Time: ")
              .append(get(next, "startTime"))
              .append(" - ")
              .append(get(next, "endTime"))
              .append("\n")
              .append("  Date: ")
              .append(get(next, "date"))
              .append("\n");
        }

        if (sb.toString().equals("Active Slots:\n"))
            return "No active or upcoming slots found.";

        return sb.toString();
    }

    @Override
    public String getSlotHistory(String token) {
        JsonNode d = apiClient.getSlotHistory(token);
        log.info("getSlotHistory: {}", d);
        if (d == null)
            return "Unable to fetch slot history.";

        return "Slot History:\n"
             + "Total Slots: "
             + get(d, "totalSlots") + "\n"
             + "Total Earnings: Rs."
             + get(d, "totalEarnings");
    }

	/*
	 * // ── PROFILE ───────────────────────────────────
	 * 
	 * @Override public String getRiderProfile(String token) { JsonNode d =
	 * apiClient .getRiderProfile(token); log.info("getRiderProfile: {}", d); if (d
	 * == null) return "Unable to fetch profile.";
	 * 
	 * JsonNode data = d.has("data") ? d.path("data") : d;
	 * 
	 * return "Profile:\n" + "Name: " + get(data, "name", "fullName", "riderName") +
	 * "\n" + "Phone: " + get(data, "phone", "phoneNumber") + "\n" + "Status: " +
	 * get(data, "status", "accountStatus") + "\n" + "Vehicle: " + get(data,
	 * "vehicleType", "vehicle"); }
	 * 
	 * @Override public String getWalletBalance(String token) { JsonNode d =
	 * apiClient.getWallet(token); log.info("getWalletBalance: {}", d); if (d ==
	 * null) return "Unable to fetch wallet.";
	 * 
	 * JsonNode data = d.has("data") ? d.path("data") : d;
	 * 
	 * return "Wallet Balance: Rs." + get(data, "balance", "walletBalance",
	 * "amount") + "\n" + "Pending: Rs." + get(data, "pending", "pendingAmount"); }
	 * 
	 * @Override public String getBankDetails(String token) { JsonNode d =
	 * apiClient.getBankDetails(token); log.info("getBankDetails: {}", d); if (d ==
	 * null) return "Unable to fetch bank details.";
	 * 
	 * JsonNode data = d.has("data") ? d.path("data") : d;
	 * 
	 * return "Bank Details:\n" + "Bank: " + get(data, "bankName", "bank") + "\n" +
	 * "Account: " + get(data, "accountNumber", "accountNo") + "\n" + "IFSC: " +
	 * get(data, "ifscCode", "ifsc") + "\n" + "Status: " + get(data,
	 * "verificationStatus", "status"); }
	 * 
	 * @Override public String getDocuments(String token) { JsonNode d =
	 * apiClient.getDocuments(token); log.info("getDocuments: {}", d); if (d ==
	 * null) return "Unable to fetch documents.";
	 * 
	 * JsonNode data = d.has("data") ? d.path("data") : d;
	 * 
	 * return "KYC Documents:\n" + "PAN: " + get(data, "panStatus", "pan") + "\n" +
	 * "Driving License: " + get(data, "dlStatus", "drivingLicense") + "\n" +
	 * "Selfie: " + get(data, "selfieStatus", "selfie"); }
	 */
    
    
 // ── PROFILE ───────────────────────────────────

    @Override
    public String getRiderProfile(String token) {
        // /api/profile/rider/profile
        // Response: { success, message, data: {...} }
        JsonNode d = apiClient.getRiderProfile(token);
        log.info("getRiderProfile: {}", d);

        if (d == null)
            return "Unable to fetch profile.";

        // data is the correct key
        JsonNode data = d.path("data");
        if (data.isMissingNode() || data.isNull())
            data = d;

        return "Profile:\n"
             + "Name: "
             + get(data, "name", "fullName",
                 "riderName") + "\n"
             + "Phone: "
             + get(data, "phone",
                 "phoneNumber") + "\n"
             + "Status: "
             + get(data, "status",
                 "accountStatus",
                 "onboardingStatus") + "\n"
             + "Vehicle: "
             + get(data, "vehicleType",
                 "vehicle", "vehicleCategory");
    }

    @Override
    public String getWalletBalance(String token) {
        // /api/profile/wallet
        // Response: { success, message, data: {...} }
        JsonNode d = apiClient.getWallet(token);
        log.info("getWalletBalance: {}", d);

        if (d == null)
            return "Unable to fetch wallet.";

        // data is the correct key
        JsonNode data = d.path("data");
        if (data.isMissingNode() || data.isNull())
            data = d;

        return "Wallet Balance:\n"
             + "Available: Rs."
             + get(data, "balance", "walletBalance",
                 "availableBalance",
                 "amount") + "\n"
             + "Pending: Rs."
             + get(data, "pending",
                 "pendingAmount",
                 "pendingBalance");
    }

    @Override
    public String getBankDetails(String token) {
        // /api/bank/bank-details
        // Response: { success, data: {...} }
        JsonNode d = apiClient.getBankDetails(token);
        log.info("getBankDetails: {}", d);

        if (d == null)
            return "Unable to fetch bank details.";

        // data is the correct key
        JsonNode data = d.path("data");
        if (data.isMissingNode() || data.isNull())
            data = d;

        // bank details may be nested further
        JsonNode bank = data.has("bankDetails")
            ? data.path("bankDetails") : data;

        return "Bank Details:\n"
             + "Bank: "
             + get(bank, "bankName", "bank") + "\n"
             + "Account: "
             + get(bank, "accountNumber",
                 "accountNo",
                 "accNumber") + "\n"
             + "IFSC: "
             + get(bank, "ifscCode",
                 "ifsc", "IFSC") + "\n"
             + "Status: "
             + get(bank, "verificationStatus",
                 "status", "isVerified");
    }

	/*
	 * @Override public String getDocuments(String token) { //
	 * /api/profile/documents // Response: { success, message, data: {...} }
	 * JsonNode d = apiClient.getDocuments(token); log.info("getDocuments: {}", d);
	 * 
	 * if (d == null) return "Unable to fetch documents.";
	 * 
	 * // data is the correct key JsonNode data = d.path("data"); if
	 * (data.isMissingNode() || data.isNull()) data = d;
	 * 
	 * // Log all keys to see structure log.info("Documents data keys: {}",
	 * data.fieldNames());
	 * 
	 * StringBuilder sb = new StringBuilder( "KYC Documents:\n");
	 * 
	 * // Try to find document status fields data.fieldNames().forEachRemaining(key
	 * -> { JsonNode doc = data.path(key); if (doc.isObject()) {
	 * sb.append(key).append(": ") .append(get(doc, "status", "verificationStatus",
	 * "isVerified")) .append("\n"); } else if (!doc.isMissingNode()) {
	 * sb.append(key).append(": ") .append(doc.asText()) .append("\n"); } });
	 * 
	 * return sb.toString().equals( "KYC Documents:\n") ?
	 * "No document information found." : sb.toString(); }
	 */
    
    @Override
    public String getDocuments(String token) {
        JsonNode d = apiClient.getDocuments(token);
        log.info("getDocuments: {}", d);

        if (d == null)
            return "Unable to fetch documents.";

        JsonNode docData = d.path("data");
        if (docData.isMissingNode() || docData.isNull())
            docData = d;

        log.info("Documents keys: {}",
                 docData.fieldNames());

        StringBuilder sb = new StringBuilder(
            "KYC Documents:\n");

        // Use iterator to avoid lambda variable conflict
        Iterator<String> keys = docData.fieldNames();
        while (keys.hasNext()) {
            String key = keys.next();
            JsonNode doc = docData.path(key);
            if (doc.isObject()) {
                sb.append(key).append(": ")
                  .append(get(doc,
                      "status",
                      "verificationStatus",
                      "isVerified"))
                  .append("\n");
            } else if (!doc.isMissingNode()
                    && !doc.isNull()) {
                sb.append(key).append(": ")
                  .append(doc.asText())
                  .append("\n");
            }
        }

        return sb.toString().equals("KYC Documents:\n")
            ? "No document information found."
            : sb.toString();
    }

    // ── INCENTIVES ────────────────────────────────

    @Override
    public String getIncentives(String token) {
        JsonNode d = apiClient.getIncentives(token);
        log.info("getIncentives: {}", d);
        if (d == null)
            return "Unable to fetch incentives.";

        JsonNode data = d.has("data")
            ? d.path("data") : d;

        StringBuilder sb = new StringBuilder(
            "Active Incentives:\n");

        JsonNode daily = data.path("daily");
        if (!daily.isMissingNode()
                && !daily.isNull()) {
            sb.append("Daily: ")
              .append(get(daily, "title",
                  "name"))
              .append(" | Progress: ")
              .append(get(daily, "progress",
                  "completed"))
              .append("/")
              .append(get(daily, "target",
                  "total"))
              .append("\n");
        }

        JsonNode weekly = data.path("weekly");
        if (!weekly.isMissingNode()
                && !weekly.isNull()) {
            sb.append("Weekly: ")
              .append(get(weekly, "title",
                  "name"))
              .append("\n");
        }

        return sb.toString().equals(
            "Active Incentives:\n")
            ? "No active incentives found."
            : sb.toString();
    }

    // ── REFERRAL ──────────────────────────────────

    @Override
    public String getReferralSummary(String token) {
        JsonNode d = apiClient
            .getReferralSummary(token);
        log.info("getReferralSummary: {}", d);
        if (d == null)
            return "Unable to fetch referral info.";

        JsonNode data = d.has("data")
            ? d.path("data") : d;

        return "Referral Summary:\n"
             + "Referral Code: "
             + get(data, "referralCode",
                 "code") + "\n"
             + "Total Referred: "
             + get(data, "totalReferred",
                 "referredCount") + "\n"
             + "Earnings: Rs."
             + get(data, "totalEarnings",
                 "referralEarnings");
    }

    // ── HELPERS ───────────────────────────────────

    private String get(JsonNode node,
            String... fields) {
        for (String f : fields) {
            JsonNode v = node.path(f);
            if (!v.isMissingNode() && !v.isNull()
                    && !v.asText().isBlank()
                    && !v.asText().equals("null")) {
                return v.asText();
            }
        }
        return "N/A";
    }

    // For nested paths like "today/totalEarnings"
    private String getAt(JsonNode node,
            String path, String... fallbacks) {
        try {
            String[] parts = path.split("/");
            JsonNode current = node;
            for (String p : parts) {
                current = current.path(p);
            }
            if (!current.isMissingNode()
                    && !current.isNull()
                    && !current.asText().isBlank()) {
                return current.asText();
            }
        } catch (Exception ignored) {}

        for (String f : fallbacks) {
            JsonNode v = node.path(f);
            if (!v.isMissingNode() && !v.isNull())
                return v.asText("N/A");
        }
        return "N/A";
    }
}