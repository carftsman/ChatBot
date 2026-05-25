
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

	/*
	 * @Override public String getPendingOrders(String token) { JsonNode d =
	 * apiClient .getPendingOrders(token); log.info("getPendingOrders raw: {}", d);
	 * if (d == null) return "Unable to fetch pending orders."; return
	 * buildOrderList("Pending Orders", d); }
	 * 
	 * @Override public String getDeliveredOrders(String token) { JsonNode d =
	 * apiClient .getDeliveredOrders(token); if (d == null) return
	 * "Unable to fetch delivered orders."; return
	 * buildOrderList("Delivered Orders", d); }
	 * 
	 * @Override public String getCancelledOrders(String token) { JsonNode d =
	 * apiClient .getCancelledOrders(token); if (d == null) return
	 * "Unable to fetch cancelled orders."; return
	 * buildOrderList("Cancelled Orders", d); }
	 */
    
	/*
	 * @Override public List<JsonNode> getPendingOrders(String token) { JsonNode d =
	 * apiClient.getPendingOrders(token); log.info("getPendingOrders raw: {}", d);
	 * return extractOrderList(d); }
	 * 
	 * @Override public List<JsonNode> getDeliveredOrders( String token) { JsonNode
	 * d = apiClient.getDeliveredOrders(token); return extractOrderList(d); }
	 * 
	 * @Override public List<JsonNode> getCancelledOrders( String token) { JsonNode
	 * d = apiClient.getCancelledOrders(token); return extractOrderList(d); }
	 * 
	 * // ── Extract raw order list from response ────────── private List<JsonNode>
	 * extractOrderList(JsonNode d) { if (d == null) return List.of();
	 * 
	 * JsonNode array = d; if (d.has("data") && d.path("data").isArray()) { array =
	 * d.path("data"); } else if (d.has("orders") && d.path("orders").isArray()) {
	 * array = d.path("orders"); } else if (d.has("content") &&
	 * d.path("content").isArray()) { array = d.path("content"); } else if
	 * (d.isArray()) { array = d; }
	 * 
	 * if (!array.isArray() || array.size() == 0) return List.of();
	 * 
	 * List<JsonNode> orders = new ArrayList<>(); int count = 0; for (JsonNode order
	 * : array) { if (count++ >= 10) break; orders.add(order); }
	 * 
	 * log.info("Returning {} orders", orders.size()); return orders; }
	 */
    
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

    	private List<Map> extractOrderList(JsonNode d) {
    	    if (d == null) return List.of();

    	    JsonNode array = d;
    	    if (d.has("data")
    	            && d.path("data").isArray()) {
    	        array = d.path("data");
    	    } else if (d.has("orders")
    	            && d.path("orders").isArray()) {
    	        array = d.path("orders");
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

    @Override
    public String getStoreDetails(String token) {
        JsonNode d = apiClient.getStoreDetails(token);
        log.info("getStoreDetails raw: {}", d);

        if (d == null)
            return "Unable to fetch store details.";

        JsonNode store = d;
        if (d.has("store")
                && d.path("store").isObject()) {
            store = d.path("store");
        } else if (d.has("data")
                && d.path("data").isObject()) {
            store = d.path("data");
        }

        String isOpen = getFirst(store,
            "isOpen", "status", "storeStatus");

        return "Store Details:\n"
             + "Name: "
             + getFirst(store, "name",
                 "storeName", "shopName") + "\n"
             + "Status: "
             + (isOpen.equalsIgnoreCase("true")
                || isOpen.equalsIgnoreCase("open")
                ? "OPEN" : isOpen) + "\n"
             + "Address: "
             + getFirst(store, "address",
                 "addressLine", "location");
    }

    @Override
    public String getWalletBalance(String token) {
        JsonNode d = apiClient.getWalletBalance(token);
        log.info("getWalletBalance raw: {}", d);

        if (d == null)
            return "Unable to fetch wallet balance.";

        JsonNode wallet = d;
        if (d.has("wallet")
                && d.path("wallet").isObject()) {
            wallet = d.path("wallet");
        } else if (d.has("data")
                && d.path("data").isObject()) {
            wallet = d.path("data");
        }

        return "Wallet Balance: Rs."
             + getFirst(wallet, "balance",
                 "walletBalance", "amount",
                 "availableBalance") + "\n"
             + "Pending: Rs."
             + getFirst(wallet, "pending",
                 "pendingAmount", "hold");
    }

    @Override
    public String getTransactionHistory(
            String token) {
        JsonNode d = apiClient
            .getTransactionHistory(token);
        if (d == null)
            return "Unable to fetch transactions.";

        JsonNode array = d;
        if (d.has("transactions")
                && d.path("transactions").isArray()) {
            array = d.path("transactions");
        } else if (d.has("data")
                && d.path("data").isArray()) {
            array = d.path("data");
        }

        if (!array.isArray() || array.size() == 0)
            return "No transactions found.";

        StringBuilder sb = new StringBuilder(
            "Recent Transactions:\n");
        int count = 0;
        for (JsonNode t : array) {
            if (count++ >= 5) break;
            sb.append("• ")
              .append(getFirst(t, "type",
                  "transactionType"))
              .append(" | Rs.")
              .append(getFirst(t, "amount",
                  "value"))
              .append(" | ")
              .append(getFirst(t, "status",
                  "transactionStatus"))
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