package com.dhatvibs.modules.consumer.serviceImpl;


import com.dhatvibs.modules.consumer.client
        .ConsumerApiClient;
import com.dhatvibs.modules.consumer.service
        .ConsumerQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerQueryServiceImpl
        implements ConsumerQueryService {

    private final ConsumerApiClient apiClient;

	/*
	 * @Override public String getRecentOrders( String token, String consumerId) {
	 * JsonNode d = apiClient .getOrdersByUser(consumerId, token);
	 * 
	 * log.info("getRecentOrders raw: {}", d);
	 * 
	 * if (d == null) return "Unable to fetch orders.";
	 * 
	 * // Try different response structures JsonNode array = d; if (d.has("orders")
	 * && d.path("orders").isArray()) { array = d.path("orders"); } else if
	 * (d.has("data") && d.path("data").isArray()) { array = d.path("data"); } else
	 * if (d.has("content") && d.path("content").isArray()) { array =
	 * d.path("content"); }
	 * 
	 * if (!array.isArray() || array.size() == 0) return "No recent orders found.";
	 * 
	 * StringBuilder sb = new StringBuilder( "Recent Orders:\n"); int count = 0; for
	 * (JsonNode o : array) { if (count++ >= 5) break;
	 * 
	 * String orderId = getFirst(o, "orderId", "id", "_id", "orderNumber"); String
	 * amount = getFirst(o, "totalAmount", "total", "amount", "grandTotal"); String
	 * status = getFirst(o, "status", "orderStatus", "state"); String store =
	 * getFirst(o, "storeName", "restaurantName", "shopName", "vendorName");
	 * 
	 * sb.append("• ").append(orderId) .append(store.equals("N/A") ? "" : " | " +
	 * store) .append(" | Rs.").append(amount) .append(" | ").append(status)
	 * .append("\n"); } return sb.toString(); }
	 */
    
	/*
	 * @Override public List<JsonNode> getRecentOrders( String token, String
	 * consumerId) {
	 * 
	 * JsonNode d = apiClient .getOrdersByUser(consumerId, token);
	 * 
	 * log.info("getRecentOrders raw keys: {}", d != null ? d.fieldNames() :
	 * "NULL");
	 * 
	 * if (d == null) return List.of();
	 * 
	 * // Find the orders array JsonNode array = d; if (d.has("orders") &&
	 * d.path("orders").isArray()) { array = d.path("orders"); } else if
	 * (d.has("data") && d.path("data").isArray()) { array = d.path("data"); } else
	 * if (d.has("content") && d.path("content").isArray()) { array =
	 * d.path("content"); } else if (d.isArray()) { array = d; }
	 * 
	 * if (!array.isArray() || array.size() == 0) {
	 * log.info("No orders found in response"); return List.of(); }
	 * 
	 * // Return raw order objects — max 10 List<JsonNode> orders = new
	 * ArrayList<>(); int count = 0; for (JsonNode order : array) { if (count++ >=
	 * 10) break; orders.add(order); }
	 * 
	 * log.info("Returning {} orders", orders.size()); return orders; }
	 */
    
    private final ObjectMapper objectMapper =
    	    new ObjectMapper();

    	@Override
    	public List<Map> getRecentOrders(
    	        String token, String consumerId) {

    	    JsonNode d = apiClient
    	        .getOrdersByUser(consumerId, token);

    	    log.info("getRecentOrders raw: {}", d);

    	    if (d == null) return List.of();

    	    JsonNode array = d;
    	    if (d.has("orders")
    	            && d.path("orders").isArray()) {
    	        array = d.path("orders");
    	    } else if (d.has("data")
    	            && d.path("data").isArray()) {
    	        array = d.path("data");
    	    } else if (d.has("content")
    	            && d.path("content").isArray()) {
    	        array = d.path("content");
    	    } else if (d.isArray()) {
    	        array = d;
    	    }

    	    if (!array.isArray() || array.size() == 0)
    	        return List.of();

    	    List<Map> orders = new ArrayList<>();
    	    int count = 0;
    	    for (JsonNode order : array) {
    	        if (count++ >= 10) break;
    	        orders.add(objectMapper
    	            .convertValue(order, Map.class));
    	    }

    	    log.info("Returning {} orders", orders.size());
    	    return orders;
    	}

    @Override
    public String getOrderDetails(
            String orderId, String token) {
        if (orderId == null || orderId.isBlank())
            return "No order selected. "
                 + "Please tap an order first.";

        JsonNode d = apiClient
            .getOrderById(orderId, token);

        log.info("getOrderDetails raw: {}", d);

        if (d == null)
            return "Unable to fetch order details.";

        // Unwrap if nested
        JsonNode order = d;
        if (d.has("order")
                && d.path("order").isObject()) {
            order = d.path("order");
        } else if (d.has("data")
                && d.path("data").isObject()) {
            order = d.path("data");
        }

        return "Order Details:\n"
             + "Order: "
             + getFirst(order, "orderId",
                 "id", "orderNumber") + "\n"
             + "Store: "
             + getFirst(order, "storeName",
                 "restaurantName", "shopName") + "\n"
             + "Status: "
             + getFirst(order, "status",
                 "orderStatus") + "\n"
             + "Amount: Rs."
             + getFirst(order, "totalAmount",
                 "total", "grandTotal");
    }

    @Override
    public String cancelOrder(
            String orderId, String token) {
        if (orderId == null || orderId.isBlank())
            return "No order selected.";

        JsonNode d = apiClient
            .cancelOrder(orderId, token);

        if (d == null)
            return "Unable to cancel order. "
                 + "Please try again or "
                 + "contact support.";

        boolean success = d.path("success")
                           .asBoolean(true);
        if (success) {
            return "Order " + orderId
                 + " has been cancelled successfully.";
        }
        return "Order cannot be cancelled at "
             + "this stage. "
             + getFirst(d, "message", "error");
    }

    @Override
    public String getOrderNotDelivered(
            String orderId, String token) {
        if (orderId == null)
            return "No order selected.";

        JsonNode d = apiClient
            .getOrderById(orderId, token);

        if (d == null)
            return "Unable to check delivery status.";

        JsonNode order = d.has("order")
            ? d.path("order") : d;

        String status = getFirst(order,
            "status", "orderStatus");

        if ("DELIVERED".equalsIgnoreCase(status)) {
            return "Our records show your order "
                 + orderId + " was delivered.\n"
                 + "If you haven't received it, "
                 + "please raise a ticket and "
                 + "our team will investigate.";
        }

        return "Order " + orderId
             + " Status: " + status + "\n"
             + "If the order is delayed, "
             + "our team will assist you.";
    }

    @Override
    public String getRefundStatus(
            String orderId, String token) {
        if (orderId == null)
            return "No order selected.";

        JsonNode d = apiClient
            .getOrderById(orderId, token);

        if (d == null)
            return "Unable to fetch refund status.";

        JsonNode order = d.has("order")
            ? d.path("order") : d;

        String status = getFirst(order,
            "paymentStatus", "refundStatus",
            "status");

        return "Refund Status for order "
             + orderId + ":\n"
             + "Status: " + status + "\n"
             + "Refunds typically take "
             + "5-7 business days.";
    }

    @Override
    public String getPaymentStatus(
            String orderId, String token) {
        if (orderId == null)
            return "No order selected.";

        JsonNode d = apiClient
            .getOrderById(orderId, token);

        if (d == null)
            return "Unable to fetch payment status.";

        JsonNode order = d.has("order")
            ? d.path("order") : d;

        return "Payment Details:\n"
             + "Order: " + orderId + "\n"
             + "Payment Status: "
             + getFirst(order, "paymentStatus",
                 "payment_status", "status") + "\n"
             + "Amount: Rs."
             + getFirst(order, "totalAmount",
                 "total", "amount");
    }

    @Override
    public String getConsumerProfile(
            String token, String consumerId) {
        // Use addresses API as profile fallback
        // since no direct profile API found
        return "Consumer ID: " + consumerId + "\n"
             + "For profile details please visit "
             + "the Profile section in the app.";
    }

    @Override
    public String getAddresses(String token) {
        JsonNode d = apiClient.getAddresses(token);

        if (d == null)
            return "Unable to fetch addresses.";

        JsonNode array = d;
        if (d.has("addresses")
                && d.path("addresses").isArray()) {
            array = d.path("addresses");
        } else if (d.has("data")
                && d.path("data").isArray()) {
            array = d.path("data");
        }

        if (!array.isArray() || array.size() == 0)
            return "No saved addresses found.";

        StringBuilder sb = new StringBuilder(
            "Saved Addresses:\n");
        int count = 0;
        for (JsonNode a : array) {
            if (count++ >= 3) break;
            sb.append("• ")
              .append(getFirst(a, "addressLine",
                  "address", "line1",
                  "fullAddress"))
              .append("\n");
        }
        return sb.toString();
    }

    private String getFirst(
            JsonNode node, String... fields) {
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
}
