
package com.dhatvibs.modules.vendor.serviceImpl;

import com.dhatvibs.modules.vendor.client
        .VendorApiClient;
import com.dhatvibs.modules.vendor.service
        .VendorQueryService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.*;

import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorQueryServiceImpl
        implements VendorQueryService {

    private final VendorApiClient apiClient;

    
    private final ObjectMapper objectMapper =
    	    new ObjectMapper();

    	@Override
    	public List<Map> getPendingOrders(String token) {
    	    JsonNode d = apiClient.getPendingOrders(token);
    	    log.info("getPendingOrders raw: {}", d);
    	    return extractOrderList(d);
    	}

    	@Override
    	public List<Map> getDeliveredOrders(String token) {
    	    JsonNode d = apiClient.getDeliveredOrders(token);
    	    return extractOrderList(d);
    	}

    	@Override
    	public List<Map> getCancelledOrders(String token) {
    	    JsonNode d = apiClient.getCancelledOrders(token);
    	    return extractOrderList(d);
    	}

		/*
		 * private List<Map> extractOrderList(JsonNode d) { if (d == null) return
		 * List.of();
		 * 
		 * JsonNode array = d; if (d.has("data") && d.path("data").isArray()) { array =
		 * d.path("data"); } else if (d.has("orders") && d.path("orders").isArray()) {
		 * array = d.path("orders"); } else if (d.has("content") &&
		 * d.path("content").isArray()) { array = d.path("content"); } else if
		 * (d.isArray()) { array = d; }
		 * 
		 * if (!array.isArray() || array.size() == 0) return List.of();
		 * 
		 * List<Map> orders = new ArrayList<>(); int count = 0; for (JsonNode order :
		 * array) { if (count++ >= 10) break; orders.add(objectMapper
		 * .convertValue(order, Map.class)); }
		 * 
		 * log.info("Returning {} orders", orders.size()); return orders; }
		 */
    	
    	private List<Map> extractOrderList(JsonNode d) {
    	    if (d == null) return List.of();

    	    log.info("extractOrderList top keys: {}",
    	             d.fieldNames());

    	    JsonNode array = null;

    	    // Try all possible array keys
    	    for (String key : new String[]{
    	            "data", "orders", "content",
    	            "items", "results", "ordersList"}) {
    	        if (d.has(key) && d.path(key).isArray()) {
    	            array = d.path(key);
    	            log.info("Found orders in key: {}",
    	                     key);
    	            break;
    	        }
    	    }

    	    // Maybe root is array
    	    if (array == null && d.isArray()) {
    	        array = d;
    	    }

    	    if (array == null || array.size() == 0) {
    	        log.info("No orders array found. "
    	               + "Full response: {}",
    	                 d.toString().substring(0,
    	                     Math.min(300,
    	                         d.toString().length())));
    	        return List.of();
    	    }

    	    List<Map> orders = new ArrayList<>();
    	    int count = 0;
    	    for (JsonNode order : array) {
    	        if (count++ >= 10) break;

    	        log.info("Vendor order node: {}",
    	                 order.toString().substring(0,
    	                     Math.min(200,
    	                         order.toString().length())));

    	        orders.add(objectMapper
    	            .convertValue(order, Map.class));
    	    }

    	    log.info("Returning {} vendor orders",
    	             orders.size());
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
             + "Customer: "
             + getFirst(order, "customerName",
                 "userName", "consumerName") + "\n"
             + "Status: "
             + getFirst(order, "status",
                 "orderStatus") + "\n"
             + "Amount: Rs."
             + getFirst(order, "totalAmount",
                 "total", "grandTotal") + "\n"
             + "Items: "
             + getFirst(order, "itemCount",
                 "totalItems", "items");
    }

	
    
    
	
    
    
	/*
	 * @Override public String getStoreDetails(String token) { JsonNode d =
	 * apiClient.getStoreDetails(token);
	 * 
	 * // Log FULL response to see exact fields
	 * log.info("getStoreDetails FULL response: {}", d != null ? d.toPrettyString()
	 * .substring(0, Math.min(800, d.toPrettyString().length())) : "NULL");
	 * 
	 * if (d == null) return "Unable to fetch store details.";
	 * 
	 * // Try to find store object in response JsonNode store = d; for (String key :
	 * new String[]{ "store", "data", "merchant", "storeDetails", "result"}) { if
	 * (d.has(key) && !d.path(key).isNull() && d.path(key).isObject()) { store =
	 * d.path(key); log.info("Found store in key: {}", key); break; } }
	 * 
	 * log.info("Store node keys: {}", store.fieldNames());
	 * 
	 * // Build response with exhaustive field checks String name = getFirst(store,
	 * "storeName", "name", "merchantName", "shopName", "title", "businessName");
	 * 
	 * String isOpen = getFirst(store, "isOpen", "open", "status", "storeStatus",
	 * "operationalStatus", "active");
	 * 
	 * // Handle boolean isOpen if ("true".equalsIgnoreCase(isOpen)) isOpen =
	 * "OPEN"; else if ("false".equalsIgnoreCase(isOpen)) isOpen = "CLOSED";
	 * 
	 * // Address may be nested object String address = "N/A"; if
	 * (!store.path("storeAddress").isMissingNode() &&
	 * store.path("storeAddress").isObject()) { JsonNode addr =
	 * store.path("storeAddress"); address = getFirst(addr, "addressLine1",
	 * "addressLine", "street", "line1", "address"); String city = getFirst(addr,
	 * "city", "district", "area"); if (!city.equals("N/A")) address += ", " + city;
	 * } else if (!store.path("address") .isMissingNode() &&
	 * store.path("address").isObject()) { JsonNode addr = store.path("address");
	 * address = getFirst(addr, "addressLine1", "addressLine", "street", "line1"); }
	 * else { address = getFirst(store, "address", "addressLine", "location",
	 * "storeAddress", "fullAddress"); }
	 * 
	 * String phone = getFirst(store, "phoneNumber", "phone", "contactNumber",
	 * "mobile", "storePhone");
	 * 
	 * return "Store Details:\n" + "Name: " + name + "\n" + "Status: " + isOpen +
	 * "\n" + "Address: " + address + "\n" + "Phone: " + phone; }
	 */
    
    @Override
    public String getStoreDetails(String token) {
        JsonNode d = apiClient.getStoreDetails(token);

        if (d == null)
            return "Unable to fetch store details.";

        JsonNode data = d.path("data");
        if (data.isMissingNode())
            data = d;

        // This API returns ORDER STATS not store info
        // Use it correctly
        JsonNode orders = data.path("orders");
        JsonNode stats  = data.path("stats");
        JsonNode rating = data.path("store_rating");

        StringBuilder sb = new StringBuilder(
            "Store Dashboard:\n");

        // Order counts
        if (!orders.isMissingNode()) {
            sb.append("Orders Today:\n")
              .append("  Pending: ")
              .append(orders.path("pending")
                            .asText("0")).append("\n")
              .append("  Ready for Pickup: ")
              .append(orders.path("ready_for_pickup")
                            .asText("0")).append("\n")
              .append("  In Transit: ")
              .append(orders.path("in_transit")
                            .asText("0")).append("\n")
              .append("  Delivered: ")
              .append(orders.path("delivered")
                            .asText("0")).append("\n")
              .append("  Cancelled: ")
              .append(orders.path("cancelled")
                            .asText("0")).append("\n");
        }

        // Revenue stats
        if (!stats.isMissingNode()) {
            sb.append("Revenue:\n")
              .append("  This Week: Rs.")
              .append(stats.at("/this_week/revenue")
                           .asText("0")).append("\n")
              .append("  This Month: Rs.")
              .append(stats.at("/this_month/revenue")
                           .asText("0")).append("\n")
              .append("  This Quarter: Rs.")
              .append(stats.at("/this_quarter/revenue")
                           .asText("0")).append("\n");
        }

        // Rating
        if (!rating.isMissingNode()) {
            sb.append("Store Rating: ")
              .append(rating.path("avg").asText("N/A"))
              .append("/5 (")
              .append(rating.path("count").asText("0"))
              .append(" reviews)");
        }

        return sb.toString();
    }

    @Override
    public String getWalletBalance(String token) {
        JsonNode d = apiClient.getWalletBalance(token);
        log.info("getWalletBalance raw: {}",
                 d != null ? d.toString()
                     .substring(0, Math.min(300,
                         d.toString().length()))
                           : "NULL");

        if (d == null)
            return "Unable to fetch wallet balance.";

        JsonNode wallet = d;
        for (String key : new String[]{
                "data", "wallet", "entitlement",
                "balance"}) {
            if (d.has(key) && d.path(key).isObject()) {
                wallet = d.path(key);
                break;
            }
        }

        return "Wallet Balance: Rs."
             + getFirst(wallet,
                 "balance", "walletBalance",
                 "amount", "availableBalance",
                 "credits") + "\n"
             + "Plan: "
             + getFirst(wallet,
                 "plan", "planName",
                 "subscriptionPlan", "tier") + "\n"
             + "Status: "
             + getFirst(wallet,
                 "status", "subscriptionStatus",
                 "isActive");
    }

    @Override
    public String getTransactionHistory(String token) {
        JsonNode d = apiClient
            .getTransactionHistory(token);
        log.info("getTransactionHistory raw: {}",
                 d != null ? d.toString()
                     .substring(0, Math.min(300,
                         d.toString().length()))
                           : "NULL");

        if (d == null)
            return "Unable to fetch transactions.";

        JsonNode array = null;
        for (String key : new String[]{
                "transactions", "data", "content",
                "items", "history"}) {
            if (d.has(key) && d.path(key).isArray()) {
                array = d.path(key);
                break;
            }
        }
        if (array == null && d.isArray())
            array = d;

        if (array == null || array.size() == 0)
            return "No transactions found.";

        StringBuilder sb = new StringBuilder(
            "Recent Transactions:\n");
        int count = 0;
        for (JsonNode t : array) {
            if (count++ >= 5) break;
            sb.append("• ")
              .append(getFirst(t,
                  "type", "transactionType",
                  "description"))
              .append(" | Rs.")
              .append(getFirst(t,
                  "amount", "value", "credits"))
              .append(" | ")
              .append(getFirst(t,
                  "status", "transactionStatus",
                  "state"))
              .append("\n");
        }
        return sb.toString();
    }

    @Override
    public String getPayoutStatus(String token) {
        return getWalletBalance(token);
    }

    private String buildOrderList(
            String title, JsonNode d) {
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
        }

        if (!array.isArray() || array.size() == 0)
            return "No " + title.toLowerCase()
                 + " found.";

        StringBuilder sb = new StringBuilder(
            title + ":\n");
        int count = 0;
        for (JsonNode o : array) {
            if (count++ >= 5) break;

            log.info("Order node: {}",
                     o.toString().substring(0,
                         Math.min(200,
                             o.toString().length())));

            sb.append("• ")
              .append(getFirst(o, "orderId",
                  "id", "_id", "orderNumber"))
              .append(" | Rs.")
              .append(getFirst(o, "totalAmount",
                  "total", "grandTotal", "amount"))
              .append(" | ")
              .append(getFirst(o, "status",
                  "orderStatus"))
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