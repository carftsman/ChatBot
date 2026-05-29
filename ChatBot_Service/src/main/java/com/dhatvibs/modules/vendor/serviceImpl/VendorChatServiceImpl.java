package com.dhatvibs.modules.vendor.serviceImpl;

import com.dhatvibs.modules.vendor.client.VendorApiClient;
import com.dhatvibs.modules.vendor.dto.*;
import com.dhatvibs.modules.vendor.entity.*;
import com.dhatvibs.modules.vendor.respository.*;
import com.dhatvibs.modules.vendor.service.*;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp
        .SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server
        .ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendorChatServiceImpl
        implements VendorChatService {

    private final VendorChatSessionRepository
                                sessionRepo;
    private final VendorChatMessageRepository
                                messageRepo;
    private final VendorTicketRepository
                                ticketRepo;
    private final VendorQueryService
                                queryService;
    private final SimpMessagingTemplate messaging;
    
    private final VendorApiClient vendorApiClient;

    @Override
    public VendorChatStartResponse startSession(
            String vendorId,
            String vendorToken,
            String orderId) {

        log.info("Vendor startSession → "
               + "vendorId: {} | orderId: {}",
                 vendorId, orderId);

        // Reuse existing open session
        if (orderId != null && !orderId.isBlank()) {
            Optional<VendorChatSession> existing =
                sessionRepo.findOpenSessionByOrderId(
                    orderId);
            if (existing.isPresent()) {
                VendorChatSession s = existing.get();
                s.setVendorToken(vendorToken);
                sessionRepo.save(s);
                log.info("Reusing session: {}",
                         s.getId());
                return VendorChatStartResponse.builder()
                    .sessionId(s.getId())
                    .status(s.getStatus())
                    .welcomeMessage(
                        "Welcome back! Continuing "
                        + "support for order "
                        + orderId)
                    .chatEnabled(s.getChatEnabled())
                    .contextOrderId(orderId)
                    .startedAt(s.getStartedAt())
                    .build();
            }
        }

        VendorChatSession session =
            VendorChatSession.builder()
                .vendorId(vendorId)
                .vendorToken(vendorToken)
                .contextOrderId(orderId)
                .status("OPEN")
                .chatEnabled(false)
                .startedAt(LocalDateTime.now())
                .build();

        session = sessionRepo.save(session);

        String welcome = (orderId != null
                && !orderId.isBlank())
            ? "Hi! I can see you selected order "
              + orderId + ".\n"
              + "Please select a category below."
            : "Hi! I am your vendor support assistant.\n"
              + "Please select a category below.";

        saveMessage(session.getId(), "SYSTEM",
            welcome, null, null, "SYSTEM");

        return VendorChatStartResponse.builder()
            .sessionId(session.getId())
            .status("OPEN")
            .welcomeMessage(welcome)
            .chatEnabled(false)
            .contextOrderId(orderId)
            .startedAt(session.getStartedAt())
            .build();
    }

	/*
	 * @Override public VendorSessionStatusResponse resolveOrEscalate( UUID
	 * sessionId, boolean resolved, String vendorId) {
	 * 
	 * VendorChatSession session = sessionRepo.findById(sessionId) .orElseThrow(()
	 * -> new ResponseStatusException( HttpStatus.NOT_FOUND, "Session not found"));
	 * 
	 * if (resolved) { session.setStatus("RESOLVED");
	 * session.setResolutionType("RESOLVED"); session.setChatEnabled(false);
	 * session.setEndedAt(LocalDateTime.now()); sessionRepo.save(session);
	 * 
	 * saveMessage(sessionId, "SYSTEM", "Issue resolved. Thank you!", null, null,
	 * "SYSTEM");
	 * 
	 * broadcast(sessionId, "Session closed. Thank you!", "SYSTEM", "RESOLVED");
	 * 
	 * return VendorSessionStatusResponse .builder() .sessionId(sessionId)
	 * .status("RESOLVED") .chatEnabled(false) .resolutionType("RESOLVED")
	 * .message("Issue resolved.") .timestamp(LocalDateTime.now()) .build();
	 * 
	 * } else { session.setChatEnabled(true);
	 * session.setResolutionType("ESCALATED"); sessionRepo.save(session);
	 * 
	 * VendorTicket ticket = VendorTicket.builder() .sessionId(sessionId)
	 * .vendorId(vendorId) .orderId( session.getContextOrderId())
	 * .category("SUPPORT") .description( "Vendor clicked Issue " + "Not Resolved.")
	 * .status("OPEN") .createdAt(LocalDateTime.now())
	 * .updatedAt(LocalDateTime.now()) .build();
	 * 
	 * VendorTicket saved = ticketRepo.save(ticket);
	 * 
	 * String ticketNum = "TKT-" + saved.getId().toString() .substring(0,
	 * 8).toUpperCase();
	 * 
	 * String msg = "Free chat enabled.\n" + "Ticket " + ticketNum + " created.\n" +
	 * "Please describe your issue.";
	 * 
	 * saveMessage(sessionId, "SYSTEM", msg, null, null, "SYSTEM");
	 * broadcast(sessionId, msg, "SYSTEM", "ESCALATED");
	 * 
	 * return VendorSessionStatusResponse .builder() .sessionId(sessionId)
	 * .status("OPEN") .chatEnabled(true) .resolutionType("ESCALATED")
	 * .message("Free chat enabled. " + "Ticket raised.")
	 * .timestamp(LocalDateTime.now()) .build(); } }
	 */
    
    @Override
    public VendorSessionStatusResponse resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String vendorId) {

        VendorChatSession session =
            sessionRepo.findById(sessionId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        if (resolved) {
            session.setStatus("RESOLVED");
            session.setResolutionType("RESOLVED");
            session.setChatEnabled(false);
            session.setEndedAt(LocalDateTime.now());
            sessionRepo.save(session);

            saveMessage(sessionId, "SYSTEM",
                "Issue resolved. Thank you!",
                null, null, "SYSTEM");

            broadcast(sessionId,
                "Session closed. Thank you!",
                "SYSTEM", "RESOLVED");

            return VendorSessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("RESOLVED")
                .chatEnabled(false)
                .resolutionType("RESOLVED")
                .message("Issue resolved.")
                .timestamp(LocalDateTime.now())
                .build();

        } else {
            session.setChatEnabled(true);
            session.setResolutionType("ESCALATED");
            sessionRepo.save(session);

            String chatSummary =
                buildChatSummary(sessionId);

            String orderNumber = null;
            String orderStatus = null;
            BigDecimal orderAmount = null;
            String storeName   = null;
            String vendorName  = null;
            String vendorPhone = null;
            String vendorEmail = null;

            String token   = session.getVendorToken();
            String orderId = session.getContextOrderId();

            if (orderId != null && token != null) {
                try {
                    JsonNode orderData =
                        vendorApiClient.getOrderById(
                            orderId, token);
                    if (orderData != null) {
                        JsonNode o = orderData.has("order")
                            ? orderData.path("order")
                            : orderData;
                        orderNumber = getField(o,
                            "orderNumber", "orderId");
                        orderStatus = getField(o,
                            "status", "orderStatus");
                        String amt = getField(o,
                            "totalAmount", "total");
                        if (!amt.equals("N/A"))
                            orderAmount =
                               new BigDecimal(amt);
                        storeName = getField(o,
                            "storeName",
                            "merchantName");
                    }

                    // Get store/vendor details
                    JsonNode storeData =
                        vendorApiClient
                            .getStoreDetails(token);
                    if (storeData != null) {
                        JsonNode store =
                            storeData.has("data")
                                ? storeData.path("data")
                                : storeData;
                        vendorName = getField(store,
                            "name", "storeName");
                        vendorPhone = getField(store,
                            "phone", "phoneNumber",
                            "contactNumber");
                        vendorEmail = getField(store,
                            "email");
                        if (storeName == null
                                || storeName.equals("N/A"))
                            storeName = vendorName;
                    }
                } catch (Exception e) {
                    log.warn("Could not fetch details: {}",
                             e.getMessage());
                }
            }

            VendorTicket ticket = VendorTicket.builder()
                .sessionId(sessionId)
                .vendorId(vendorId)
                .vendorName(vendorName)
                .vendorPhone(vendorPhone)
                .vendorEmail(vendorEmail)
                .orderId(orderId)
                .orderNumber(orderNumber)
                .orderStatus(orderStatus)
                .orderAmount(orderAmount)
                .storeName(storeName)
                .chatSummary(chatSummary)
                .category("SUPPORT")
                .description(
                    "Vendor clicked Issue Not Resolved.")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            VendorTicket saved = ticketRepo.save(ticket);

            log.info("Ticket raised: {} for vendor: {}",
                     saved.getId(), vendorId);

            String msg =
                "Your issue has been escalated.\n"
                + "Our support team will contact you "
                + "shortly.\n"
                + "Ticket ID: TKT-"
                + saved.getId().toString()
                    .substring(0, 8).toUpperCase();

            saveMessage(sessionId, "SYSTEM",
                msg, null, null, "SYSTEM");
            broadcast(sessionId, msg,
                "SYSTEM", "ESCALATED");

            return VendorSessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("OPEN")
                .chatEnabled(true)
                .resolutionType("ESCALATED")
                .message(msg)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    private String buildChatSummary(UUID sessionId) {
        List<VendorChatMessage> messages =
            messageRepo.findBySessionIdOrderBySentAtAsc(
                sessionId);
        StringBuilder sb = new StringBuilder();
        for (VendorChatMessage m : messages) {
            if ("SYSTEM".equals(m.getSenderType()))
                continue;
            sb.append(m.getSenderType())
              .append(": ")
              .append(m.getMessage())
              .append("\n");
        }
        return sb.toString();
    }

    private String getField(
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

    @Override
    public VendorWebSocketResponse processMessage(
            VendorWebSocketRequest request) {

        VendorChatSession session =
            sessionRepo.findById(
                    request.getSessionId())
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        if (!Boolean.TRUE.equals(
                session.getChatEnabled())) {
            VendorWebSocketResponse blocked =
                VendorWebSocketResponse.builder()
                    .reply("Please use FAQ options.")
                    .sessionId(session.getId())
                    .senderType("SYSTEM")
                    .status("OPEN")
                    .timestamp(LocalDateTime.now())
                    .build();
            messaging.convertAndSend(
                "/topic/vendor/"
                + session.getId(), blocked);
            return blocked;
        }

        saveMessage(session.getId(), "USER",
            request.getMessage(),
            null, null, "FREE_CHAT");

        String reply = resolveFreeChatMessage(
            request.getMessage(),
            session.getVendorToken(),
            session.getContextOrderId());

        saveMessage(session.getId(), "BOT",
            reply, null, null, "FREE_CHAT");

        VendorWebSocketResponse response =
            VendorWebSocketResponse.builder()
                .reply(reply)
                .sessionId(session.getId())
                .senderType("BOT")
                .status("OPEN")
                .timestamp(LocalDateTime.now())
                .build();

        messaging.convertAndSend(
            "/topic/vendor/"
            + session.getId(), response);

        return response;
    }

	
    
    
    @Override
    public VendorOrderHistoryResponse getHistoryByOrderId(
            String orderId,
            int page,
            int size) {

        List<VendorChatSession> sessions =
            sessionRepo.findAllByOrderId(orderId);

        if (sessions.isEmpty())
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No chat history for order: " + orderId);

        String latestStatus =
            sessions.get(0).getStatus();

        // Merge ALL messages from ALL sessions
        List<VendorOrderHistoryResponse.MessageDto>
            allMessages = sessions.stream()
                .flatMap(s ->
                    messageRepo
                        .findBySessionIdOrderBySentAtAsc(
                            s.getId())
                        .stream()
                        .map(m ->
                            VendorOrderHistoryResponse
                                .MessageDto.builder()
                                .id(m.getId())
                                .senderType(
                                    m.getSenderType())
                                .message(m.getMessage())
                                .messageType(
                                    m.getMessageType())
                                .intent(m.getIntent())
                                .sentAt(m.getSentAt())
                                .sessionId(
                                    s.getId().toString())
                                .build()))
                .sorted(Comparator.comparing(
                    VendorOrderHistoryResponse
                        .MessageDto::getSentAt))
                .collect(Collectors.toList());

        long totalMessages = allMessages.size();
        int  totalPages    = (int) Math.ceil(
            (double) totalMessages / size);

        // Apply pagination
        int fromIndex = page * size;
        int toIndex   = Math.min(
            fromIndex + size, (int) totalMessages);

        List<VendorOrderHistoryResponse.MessageDto>
            pagedMessages =
                fromIndex >= totalMessages
                    ? List.of()
                    : allMessages.subList(
                        fromIndex, toIndex);

        return VendorOrderHistoryResponse.builder()
            .orderId(orderId)
            .latestStatus(latestStatus)
            .page(page)
            .size(size)
            .totalMessages(totalMessages)
            .totalPages(totalPages)
            .hasNext(page + 1 < totalPages)
            .hasPrevious(page > 0)
            .messages(pagedMessages)
            .build();
    }

	/*
	 * private String resolveFreeChatMessage( String message, String token, String
	 * orderId) {
	 * 
	 * String lower = message.toLowerCase().trim();
	 * 
	 * if (lower.contains("pending")) return queryService.getPendingOrders(token);
	 * if (lower.contains("deliver")) return queryService.getDeliveredOrders(token);
	 * if (lower.contains("cancel")) return queryService.getCancelledOrders(token);
	 * if (lower.contains("payout") || lower.contains("payment")) return
	 * queryService.getPayoutStatus(token); if (lower.contains("store") ||
	 * lower.contains("shop")) return queryService.getStoreDetails(token); if
	 * (lower.contains("wallet") || lower.contains("balance")) return
	 * queryService.getWalletBalance(token); if (lower.contains("order")) return
	 * queryService.getOrderDetails( orderId, token);
	 * 
	 * return "I am here to help. " + "Please describe your issue."; }
	 */
    
	/*
	 * private String resolveFreeChatMessage( String message, String token, String
	 * orderId) {
	 * 
	 * String lower = message.toLowerCase().trim();
	 * 
	 * if (lower.contains("pending")) { List<JsonNode> orders =
	 * queryService.getPendingOrders(token); return orders.isEmpty() ?
	 * "No pending orders found." : orders.size() + " pending order(s) found."; } if
	 * (lower.contains("deliver")) { List<JsonNode> orders =
	 * queryService.getDeliveredOrders(token); return orders.isEmpty() ?
	 * "No delivered orders found." : orders.size() + " delivered order(s) found.";
	 * } if (lower.contains("cancel")) { List<JsonNode> orders =
	 * queryService.getCancelledOrders(token); return orders.isEmpty() ?
	 * "No cancelled orders found." : orders.size() + " cancelled order(s) found.";
	 * } if (lower.contains("payout") || lower.contains("payment")) return
	 * queryService.getPayoutStatus(token); if (lower.contains("store") ||
	 * lower.contains("shop")) return queryService.getStoreDetails(token); if
	 * (lower.contains("wallet") || lower.contains("balance")) return
	 * queryService.getWalletBalance(token); if (lower.contains("order")) return
	 * queryService.getOrderDetails( orderId, token);
	 * 
	 * return "I am here to help. " + "Please describe your issue."; }
	 */
    
    private String resolveFreeChatMessage(
            String message, String token,
            String orderId) {

        String lower = message.toLowerCase().trim();

        if (lower.contains("pending")) {
            List<Map> orders =
                queryService.getPendingOrders(token);
            return orders.isEmpty()
                ? "No pending orders found."
                : orders.size() + " pending order(s).";
        }
        if (lower.contains("deliver")) {
            List<Map> orders =
                queryService.getDeliveredOrders(token);
            return orders.isEmpty()
                ? "No delivered orders found."
                : orders.size() + " delivered order(s).";
        }
        if (lower.contains("cancel")) {
            List<Map> orders =
                queryService.getCancelledOrders(token);
            return orders.isEmpty()
                ? "No cancelled orders found."
                : orders.size() + " cancelled order(s).";
        }
        if (lower.contains("payout")
                || lower.contains("payment"))
            return queryService.getPayoutStatus(token);
        if (lower.contains("store")
                || lower.contains("shop"))
            return queryService.getStoreDetails(token);
        if (lower.contains("wallet")
                || lower.contains("balance"))
            return queryService.getWalletBalance(token);
        if (lower.contains("order"))
            return queryService.getOrderDetails(
                orderId, token);

        return "I am here to help. "
             + "Please describe your issue.";
    }

    private void saveMessage(
            UUID sessionId, String senderType,
            String message, String intent,
            UUID faqId, String messageType) {
        messageRepo.save(
            VendorChatMessage.builder()
                .sessionId(sessionId)
                .senderType(senderType)
                .message(message)
                .intent(intent)
                .matchedFaqId(faqId)
                .messageType(messageType)
                .sentAt(LocalDateTime.now())
                .build());
    }

    private void broadcast(
            UUID sessionId, String reply,
            String senderType, String status) {
        messaging.convertAndSend(
            "/topic/vendor/" + sessionId,
            VendorWebSocketResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .senderType(senderType)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build());
    }
}