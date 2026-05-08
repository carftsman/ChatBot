package com.dhatvibs.modules.serviceImpl.chat;



/*
 * import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
 * import org.springframework.stereotype.Service;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatRequest; import
 * com.dhatvibs.modules.dto.chat.ChatResponse; import
 * com.dhatvibs.modules.dto.chat.SessionResponse; import
 * com.dhatvibs.modules.entities.auth.CbUser; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.entities.chat.CbChatSession; import
 * com.dhatvibs.modules.entities.chat.CbFaq; import
 * com.dhatvibs.modules.entities.chat.CbTicket; import
 * com.dhatvibs.modules.repository.auth.CbUserRepository; import
 * com.dhatvibs.modules.repository.chat.*; import
 * com.dhatvibs.modules.service.chat.*;
 * 
 * import java.time.LocalDateTime; import java.util.*;
 * 
 * @Slf4j
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class ChatServiceImpl implements ChatService
 * {
 * 
 * private final IntentDetectorService intentDetector; private final
 * OrderQueryService orderQueryService; private final PaymentQueryService
 * paymentQueryService; private final CbChatSessionRepository sessionRepo;
 * private final CbChatMessageRepository messageRepo; private final
 * CbTicketRepository ticketRepo; private final CbUserRepository userRepo;
 * 
 * @Override public SessionResponse startSession( String externalUserId, String
 * appId) {
 * 
 * CbUser user = userRepo .findByExternalUserIdAndAppId( externalUserId, appId)
 * .orElseThrow(() -> new RuntimeException("User not found"));
 * 
 * CbChatSession session = CbChatSession.builder() .cbUserId(user.getId())
 * .appId(appId) .status("OPEN") .startedAt(LocalDateTime.now()) .build();
 * 
 * session = sessionRepo.save(session);
 * 
 * // Save welcome message from BOT saveMessage(session.getId(), "BOT",
 * getWelcomeMessage(appId), null, null);
 * 
 * return SessionResponse.builder() .sessionId(session.getId()) .appId(appId)
 * .status("OPEN") .startedAt(session.getStartedAt()) .build(); }
 * 
 * @Override public ChatResponse processMessage( ChatRequest request, String
 * externalUserId, String appId) {
 * 
 * // Get or create session UUID sessionId = request.getSessionId();
 * CbChatSession session;
 * 
 * if (sessionId == null) { SessionResponse s = startSession(externalUserId,
 * appId); sessionId = s.getSessionId(); session =
 * sessionRepo.findById(sessionId) .orElseThrow(); } else { session =
 * sessionRepo .findByIdAndStatus(sessionId, "OPEN") .orElseThrow(() -> new
 * RuntimeException( "Session not found or closed")); }
 * 
 * // Save user message saveMessage(sessionId, "USER", request.getMessage(),
 * null, null);
 * 
 * // Detect intent Optional<CbFaq> faqOpt = intentDetector.detect(
 * request.getMessage(), appId);
 * 
 * if (faqOpt.isEmpty()) { // No FAQ match — create ticket String reply =
 * raiseTicket( session, externalUserId, appId, request.getMessage());
 * saveMessage(sessionId, "BOT", reply, "fallback", null); return
 * buildResponse(reply, "fallback", sessionId, "ESCALATED"); }
 * 
 * CbFaq faq = faqOpt.get(); String reply;
 * 
 * // If needs DB — query live data if (Boolean.TRUE.equals(faq.getNeedsDb())) {
 * reply = resolveDynamicAnswer( faq.getIntent(), externalUserId, appId); } else
 * { reply = faq.getAnswer(); }
 * 
 * // If fallback intent — auto create ticket if
 * ("fallback".equals(faq.getIntent())) { raiseTicket(session, externalUserId,
 * appId, request.getMessage()); }
 * 
 * saveMessage(sessionId, "BOT", reply, faq.getIntent(), faq.getId());
 * 
 * return buildResponse(reply, faq.getIntent(), sessionId, "OPEN"); }
 * 
 * @Override public void endSession(UUID sessionId) {
 * sessionRepo.findById(sessionId).ifPresent(s -> { s.setStatus("RESOLVED");
 * s.setEndedAt(LocalDateTime.now()); sessionRepo.save(s); }); }
 * 
 * @Override public List<CbChatMessage> getHistory(UUID sessionId) { return
 * messageRepo .findBySessionIdOrderBySentAtAsc(sessionId); }
 * 
 * // ───────────────────────────────────────────── // Route intent to correct
 * DB query // ───────────────────────────────────────────── private String
 * resolveDynamicAnswer( String intent, String externalUserId, String appId) {
 * 
 * return switch (intent) {
 * 
 * // USER intents case "track_order", "order_status", "delivery_delay",
 * "order_not_delivered" -> orderQueryService
 * .getActiveOrderStatus(externalUserId);
 * 
 * case "order_history" -> orderQueryService .getOrderHistory(externalUserId,
 * appId);
 * 
 * case "order_timeline" -> orderQueryService .getOrderTimeline(externalUserId,
 * appId);
 * 
 * case "refund_status" -> paymentQueryService .getRefundStatus(externalUserId,
 * appId);
 * 
 * case "money_deducted", "payment_failed" -> paymentQueryService
 * .getPaymentFailureStatus( externalUserId, appId);
 * 
 * // VENDOR intents case "new_order", "order_preparation_time",
 * "vendor_cancel_order", "customer_cancelled" -> orderQueryService
 * .getVendorActiveOrder(externalUserId);
 * 
 * case "vendor_order_history" -> orderQueryService
 * .getOrderHistory(externalUserId, appId);
 * 
 * case "rider_not_arrived" -> orderQueryService
 * .getVendorActiveOrder(externalUserId);
 * 
 * case "payout_status", "payout_not_received" -> paymentQueryService
 * .getLatestPaymentStatus( externalUserId, appId);
 * 
 * // RIDER intents case "current_order", "order_picked_status",
 * "delivery_completed" -> orderQueryService
 * .getRiderActiveOrder(externalUserId);
 * 
 * case "earnings_today", "payout_rider", "payment_not_received_rider" ->
 * paymentQueryService .getLatestPaymentStatus( externalUserId, appId);
 * 
 * default -> "I am looking into this. " + "Please hold on."; }; }
 * 
 * private String raiseTicket( CbChatSession session, String externalUserId,
 * String appId, String message) {
 * 
 * CbUser user = userRepo .findByExternalUserIdAndAppId( externalUserId, appId)
 * .orElse(null);
 * 
 * CbTicket ticket = CbTicket.builder() .sessionId(session.getId())
 * .cbUserId(user != null ? user.getId() : null) .appId(appId)
 * .category("GENERAL") .description(message) .status("OPEN")
 * .createdAt(LocalDateTime.now()) .updatedAt(LocalDateTime.now()) .build();
 * 
 * ticketRepo.save(ticket);
 * 
 * return "🎫 A support ticket has been created " + "for your query.\n" +
 * "Our team will contact you within " + (appId.equals("RIDER") ? "1 hour" :
 * appId.equals("VENDOR") ? "4 hours" : "2 hours") +
 * ".\nIs there anything else I can help you with?"; }
 * 
 * private void saveMessage( UUID sessionId, String senderType, String message,
 * String intent, UUID faqId) {
 * 
 * messageRepo.save(CbChatMessage.builder() .sessionId(sessionId)
 * .senderType(senderType) .message(message) .intent(intent)
 * .matchedFaqId(faqId) .sentAt(LocalDateTime.now()) .build()); }
 * 
 * private String getWelcomeMessage(String appId) { return switch (appId) { case
 * "USER" -> "👋 Hi! I am your support assistant.\n" +
 * "I can help you with orders, payments, " +
 * "delivery, and more.\nWhat can I help you with?"; case "VENDOR" ->
 * "👋 Hi! I am your vendor support assistant.\n" +
 * "I can help with orders, payments, " +
 * "store settings, and more.\nHow can I assist you?"; case "RIDER" ->
 * "👋 Hi! I am your delivery support assistant.\n" +
 * "I can help with deliveries, earnings, " +
 * "and app issues.\nWhat do you need help with?"; default ->
 * "👋 Hi! How can I help you today?"; }; }
 * 
 * private ChatResponse buildResponse( String reply, String intent, UUID
 * sessionId, String status) { return ChatResponse.builder() .reply(reply)
 * .intent(intent) .sessionId(sessionId) .status(status)
 * .timestamp(LocalDateTime.now()) .build(); } }
 */  


/*
 * import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
 * import org.springframework.messaging.simp.SimpMessagingTemplate; import
 * org.springframework.stereotype.Service; import
 * org.springframework.http.HttpStatus; import
 * org.springframework.web.server.ResponseStatusException;
 * 
 * import com.dhatvibs.modules.dto.chat.ChatStartResponse; import
 * com.dhatvibs.modules.dto.chat.WebSocketChatRequest; import
 * com.dhatvibs.modules.dto.chat.WebSocketChatResponse; import
 * com.dhatvibs.modules.entities.auth.CbUser; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.entities.chat.CbChatSession; import
 * com.dhatvibs.modules.entities.chat.CbFaq; import
 * com.dhatvibs.modules.entities.chat.CbOrder; import
 * com.dhatvibs.modules.entities.chat.CbTicket; import
 * com.dhatvibs.modules.repository.auth.CbUserRepository; import
 * com.dhatvibs.modules.repository.chat.CbChatMessageRepository; import
 * com.dhatvibs.modules.repository.chat.CbChatSessionRepository; import
 * com.dhatvibs.modules.repository.chat.CbOrderRepository; import
 * com.dhatvibs.modules.repository.chat.CbTicketRepository; import
 * com.dhatvibs.modules.service.chat.ChatService; import
 * com.dhatvibs.modules.service.chat.IntentDetectorService; import
 * com.dhatvibs.modules.service.chat.OrderQueryService; import
 * com.dhatvibs.modules.service.chat.PaymentQueryService;
 * 
 * import java.time.LocalDateTime; import java.util.*;
 * 
 * @Slf4j
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class ChatServiceImpl implements ChatService
 * {
 * 
 * private final IntentDetectorService intentDetector; private final
 * OrderQueryService orderQueryService; private final PaymentQueryService
 * paymentQueryService; private final CbChatSessionRepository sessionRepo;
 * private final CbChatMessageRepository messageRepo; private final
 * CbTicketRepository ticketRepo; private final CbUserRepository userRepo;
 * private final CbOrderRepository orderRepo;
 * 
 * // WebSocket broadcast template private final SimpMessagingTemplate
 * messagingTemplate;
 * 
 * // ───────────────────────────────────────────── // START SESSION //
 * ─────────────────────────────────────────────
 * 
 * @Override public ChatStartResponse startSession( String externalUserId,
 * String appId, UUID orderId) {
 * 
 * // Find user CbUser user = userRepo .findByExternalUserIdAndAppId(
 * externalUserId, appId) .orElseThrow(() -> new ResponseStatusException(
 * HttpStatus.NOT_FOUND, "User not found"));
 * 
 * // Validate orderId if provided CbOrder order = null; if (orderId != null) {
 * order = orderRepo.findById(orderId) .orElseThrow(() -> new
 * ResponseStatusException( HttpStatus.NOT_FOUND, "Order not found")); }
 * 
 * // Create session with order context CbChatSession session =
 * CbChatSession.builder() .cbUserId(user.getId()) .appId(appId) .status("OPEN")
 * .contextOrderId(orderId) // store orderId in session
 * .startedAt(LocalDateTime.now()) .build();
 * 
 * session = sessionRepo.save(session);
 * 
 * // Build welcome message String welcome = buildWelcomeMessage( appId, order);
 * 
 * // Save welcome message from BOT saveMessage(session.getId(), "BOT", welcome,
 * null, null);
 * 
 * log.info("Session started → sessionId: {} | " + "userId: {} | orderId: {}",
 * session.getId(), externalUserId, orderId);
 * 
 * return ChatStartResponse.builder() .sessionId(session.getId()) .appId(appId)
 * .status("OPEN") .welcomeMessage(welcome) .contextOrderId(orderId)
 * .startedAt(session.getStartedAt()) .build(); }
 * 
 * // ───────────────────────────────────────────── // PROCESS MESSAGE —
 * WebSocket // ─────────────────────────────────────────────
 * 
 * @Override public WebSocketChatResponse processMessage( WebSocketChatRequest
 * request) {
 * 
 * // Load session CbChatSession session = sessionRepo .findByIdAndStatus(
 * request.getSessionId(), "OPEN") .orElseThrow(() -> new
 * ResponseStatusException( HttpStatus.NOT_FOUND,
 * "Session not found or already closed"));
 * 
 * // Save USER message saveMessage(session.getId(), "USER",
 * request.getMessage(), null, null);
 * 
 * // Detect intent Optional<CbFaq> faqOpt = intentDetector
 * .detect(request.getMessage(), request.getAppId());
 * 
 * String reply; String intent = "fallback"; String status = "OPEN";
 * 
 * if (faqOpt.isPresent()) { CbFaq faq = faqOpt.get(); intent = faq.getIntent();
 * 
 * if (Boolean.TRUE.equals(faq.getNeedsDb())) { // Use session orderId context
 * // if available — KEY part reply = resolveDynamicAnswer( faq.getIntent(),
 * request.getUserId(), request.getAppId(), session.getContextOrderId()); } else
 * { reply = faq.getAnswer(); }
 * 
 * // Auto raise ticket for fallback if ("fallback".equals(faq.getIntent())) {
 * raiseTicket(session, request.getUserId(), request.getAppId(),
 * request.getMessage()); status = "ESCALATED"; }
 * 
 * saveMessage(session.getId(), "BOT", reply, intent, faq.getId());
 * 
 * } else { // No match — raise ticket reply = raiseTicket(session,
 * request.getUserId(), request.getAppId(), request.getMessage()); status =
 * "ESCALATED"; saveMessage(session.getId(), "BOT", reply, "fallback", null); }
 * 
 * // Build response WebSocketChatResponse response =
 * WebSocketChatResponse.builder() .reply(reply) .intent(intent)
 * .sessionId(session.getId()) .senderType("BOT") .status(status)
 * .timestamp(LocalDateTime.now()) .build();
 * 
 * // Broadcast to WebSocket topic // Frontend subscribes to this topic
 * messagingTemplate.convertAndSend( "/topic/session/" + session.getId(),
 * response);
 * 
 * log.info("Message processed → sessionId: {} " + "| intent: {}",
 * session.getId(), intent);
 * 
 * return response; }
 * 
 * // ───────────────────────────────────────────── // END SESSION //
 * ─────────────────────────────────────────────
 * 
 * @Override public void endSession(UUID sessionId) {
 * sessionRepo.findById(sessionId) .ifPresent(s -> { s.setStatus("RESOLVED");
 * s.setEndedAt(LocalDateTime.now()); sessionRepo.save(s);
 * log.info("Session ended → {}", sessionId); }); }
 * 
 * // ───────────────────────────────────────────── // GET HISTORY //
 * ─────────────────────────────────────────────
 * 
 * @Override public List<CbChatMessage> getHistory( UUID sessionId) { return
 * messageRepo .findBySessionIdOrderBySentAtAsc( sessionId); }
 * 
 * // ───────────────────────────────────────────── // RESOLVE DYNAMIC ANSWER //
 * Uses contextOrderId if available //
 * ───────────────────────────────────────────── private String
 * resolveDynamicAnswer( String intent, String externalUserId, String appId,
 * UUID contextOrderId) {
 * 
 * return switch (intent) {
 * 
 * // ORDER intents case "track_order", "order_status", "delivery_delay",
 * "order_not_delivered" -> orderQueryService.getOrderStatus( externalUserId,
 * appId, contextOrderId);
 * 
 * case "order_history" -> orderQueryService.getOrderHistory( externalUserId,
 * appId);
 * 
 * case "order_timeline" -> orderQueryService.getOrderTimeline( externalUserId,
 * appId, contextOrderId);
 * 
 * case "cancel_order" -> orderQueryService.checkCancelEligibility(
 * externalUserId, appId, contextOrderId);
 * 
 * // PAYMENT intents case "refund_status" ->
 * paymentQueryService.getRefundStatus( externalUserId, appId, contextOrderId);
 * 
 * case "money_deducted", "payment_failed" -> paymentQueryService
 * .getPaymentFailureStatus( externalUserId, appId, contextOrderId);
 * 
 * // VENDOR intents case "new_order", "order_preparation_time",
 * "vendor_cancel_order", "customer_cancelled", "rider_not_arrived" ->
 * orderQueryService.getOrderStatus( externalUserId, appId, contextOrderId);
 * 
 * case "vendor_order_history" -> orderQueryService.getOrderHistory(
 * externalUserId, appId);
 * 
 * case "payout_status", "payout_not_received" -> paymentQueryService
 * .getLatestPaymentStatus( externalUserId, appId, contextOrderId);
 * 
 * // RIDER intents case "current_order", "order_picked_status",
 * "delivery_completed" -> orderQueryService.getOrderStatus( externalUserId,
 * appId, contextOrderId);
 * 
 * case "earnings_today", "payout_rider", "payment_not_received_rider" ->
 * paymentQueryService .getLatestPaymentStatus( externalUserId, appId,
 * contextOrderId);
 * 
 * default -> "I am looking into this. Please hold."; }; }
 * 
 * // ───────────────────────────────────────────── // RAISE TICKET //
 * ───────────────────────────────────────────── private String raiseTicket(
 * CbChatSession session, String externalUserId, String appId, String message) {
 * 
 * CbUser user = userRepo .findByExternalUserIdAndAppId( externalUserId, appId)
 * .orElse(null);
 * 
 * CbTicket ticket = CbTicket.builder() .sessionId(session.getId())
 * .cbUserId(user != null ? user.getId() : null)
 * .cbOrderId(session.getContextOrderId()) .appId(appId) .category("GENERAL")
 * .description(message) .status("OPEN") .createdAt(LocalDateTime.now())
 * .updatedAt(LocalDateTime.now()) .build();
 * 
 * ticketRepo.save(ticket);
 * 
 * return "🎫 Support ticket created for your query.\n" +
 * "Our team will contact you within " + (appId.equals("RIDER") ? "1 hour" :
 * appId.equals("VENDOR") ? "4 hours" : "2 hours") +
 * ".\nAnything else I can help you with?"; }
 * 
 * // ───────────────────────────────────────────── // SAVE MESSAGE //
 * ───────────────────────────────────────────── private void saveMessage( UUID
 * sessionId, String senderType, String message, String intent, UUID faqId) {
 * 
 * messageRepo.save(CbChatMessage.builder() .sessionId(sessionId)
 * .senderType(senderType) .message(message) .intent(intent)
 * .matchedFaqId(faqId) .sentAt(LocalDateTime.now()) .build()); }
 * 
 * // ───────────────────────────────────────────── // WELCOME MESSAGE //
 * ───────────────────────────────────────────── private String
 * buildWelcomeMessage( String appId, CbOrder order) {
 * 
 * if (order != null) { // Order-specific welcome return switch (appId) { case
 * "USER" -> "👋 Hi! I can see your order from " + order.getRestaurantName() +
 * " worth ₹" + order.getTotalAmount() + " (Status: " + order.getOrderStatus() +
 * ").\n" + "What issue are you facing " + "with this order?";
 * 
 * case "VENDOR" -> "👋 Hi! I can see order " + order.getExternalOrderId() +
 * " (Status: " + order.getOrderStatus() + ").\n" + "What issue are you facing "
 * + "with this order?";
 * 
 * case "RIDER" -> "👋 Hi! I can see your delivery " +
 * order.getExternalOrderId() + " (Status: " + order.getOrderStatus() + ").\n" +
 * "What issue are you facing?";
 * 
 * default -> "👋 Hi! How can I help you today?"; }; }
 * 
 * // Generic welcome — no order selected return switch (appId) { case "USER" ->
 * "👋 Hi! I am your support assistant.\n" +
 * "I can help you with orders, payments, " + "delivery and more.\n" +
 * "What can I help you with?"; case "VENDOR" ->
 * "👋 Hi! I am your vendor support assistant.\n" +
 * "I can help with orders, payments " + "and store settings.\n" +
 * "How can I assist you?"; case "RIDER" ->
 * "👋 Hi! I am your delivery support assistant.\n" +
 * "I can help with deliveries, earnings " + "and app issues.\n" +
 * "What do you need help with?"; default -> "👋 Hi! How can I help you today?";
 * }; } }
 */  




import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dhatvibs.modules.dto.chat.*;
import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbChatMessage;
import com.dhatvibs.modules.entities.chat.CbChatSession;
import com.dhatvibs.modules.entities.chat.CbFaq;
import com.dhatvibs.modules.entities.chat.CbOrder;
import com.dhatvibs.modules.entities.chat.CbTicket;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.*;
import com.dhatvibs.modules.service.chat.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final CbChatSessionRepository  sessionRepo;
    private final CbChatMessageRepository  messageRepo;
    private final CbTicketRepository       ticketRepo;
    private final CbUserRepository         userRepo;
    private final CbOrderRepository        orderRepo;
    private final IntentDetectorService    intentDetector;
    private final OrderQueryService        orderQueryService;
    private final PaymentQueryService      paymentQueryService;
    private final SimpMessagingTemplate    messagingTemplate;

    // ─────────────────────────────────────────────
    // START SESSION
    // chatEnabled = FALSE by default
    // ─────────────────────────────────────────────
    @Override
    public ChatStartResponse startSession(
            String externalUserId,
            String appId,
            UUID orderId) {

        CbUser user = findUser(externalUserId, appId);

        CbOrder order = null;
        if (orderId != null) {
            order = orderRepo.findById(orderId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Order not found"));
        }

        // Create session — chat NOT enabled yet
        CbChatSession session = CbChatSession.builder()
            .cbUserId(user.getId())
            .appId(appId)
            .status("OPEN")
            .contextOrderId(orderId)
            .chatEnabled(false) // FAQ only initially
            .startedAt(LocalDateTime.now())
            .build();

        session = sessionRepo.save(session);

        // Save system welcome message
        String welcome = buildWelcomeMessage(
            appId, order);
        saveMessage(session.getId(), "SYSTEM",
            welcome, null, null, "SYSTEM");

        log.info("Session started → {} | chatEnabled: false",
                 session.getId());

        return ChatStartResponse.builder()
            .sessionId(session.getId())
            .appId(appId)
            .status("OPEN")
            .welcomeMessage(welcome)
            .contextOrderId(orderId)
            .startedAt(session.getStartedAt())
            .build();
    }

    // ─────────────────────────────────────────────
    // RESOLVE OR ESCALATE
    // Called when user taps:
    // ✅ "Issue Resolved"     → resolved = true
    // ❌ "Issue Not Resolved" → resolved = false
    // ─────────────────────────────────────────────
    @Override
    public SessionStatusResponse resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String externalUserId,
            String appId) {

        CbChatSession session = sessionRepo
            .findById(sessionId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Session not found"));

        if (resolved) {
            // ✅ User happy — close session
            session.setStatus("RESOLVED");
            session.setResolutionType("RESOLVED");
            session.setChatEnabled(false);
            session.setEndedAt(LocalDateTime.now());
            sessionRepo.save(session);

            // Save system message
            saveMessage(session.getId(), "SYSTEM",
                "✅ Your issue has been marked as resolved. "
              + "Thank you for contacting support!",
                null, null, "SYSTEM");

            // Broadcast session closed via WebSocket
            messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                WebSocketChatResponse.builder()
                    .reply("Session closed. Thank you!")
                    .sessionId(sessionId)
                    .senderType("SYSTEM")
                    .status("RESOLVED")
                    .timestamp(LocalDateTime.now())
                    .build());

            log.info("Session RESOLVED → {}", sessionId);

            return SessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("RESOLVED")
                .chatEnabled(false)
                .resolutionType("RESOLVED")
                .message("Issue resolved. Session closed.")
                .timestamp(LocalDateTime.now())
                .build();

        } else {
            // ❌ User not happy — enable free chat
            session.setChatEnabled(true);
            session.setResolutionType("ESCALATED");
            sessionRepo.save(session);

            // Save system message enabling chat
            String escalateMsg =
                "💬 Free chat is now enabled.\n"
              + "Please describe your issue in detail "
              + "and our support will assist you.";

            saveMessage(session.getId(), "SYSTEM",
                escalateMsg, null, null, "SYSTEM");

            // Broadcast chat enabled via WebSocket
            messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId,
                WebSocketChatResponse.builder()
                    .reply(escalateMsg)
                    .sessionId(sessionId)
                    .senderType("SYSTEM")
                    .status("ESCALATED")
                    .timestamp(LocalDateTime.now())
                    .build());

            log.info("Chat ENABLED → {}", sessionId);

            return SessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("OPEN")
                .chatEnabled(true)
                .resolutionType("ESCALATED")
                .message("Free chat enabled. "
                       + "You can now type your message.")
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    // ─────────────────────────────────────────────
    // PROCESS FREE CHAT MESSAGE (WebSocket)
    // Only works when chatEnabled = TRUE
    // ─────────────────────────────────────────────
    @Override
    public WebSocketChatResponse processMessage(
            WebSocketChatRequest request) {

        CbChatSession session = sessionRepo
            .findById(request.getSessionId())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Session not found"));

        // Guard — chat must be enabled
        if (!Boolean.TRUE.equals(
                session.getChatEnabled())) {
            WebSocketChatResponse blocked =
                WebSocketChatResponse.builder()
                    .reply("❌ Free chat is not enabled yet. "
                         + "Please use the FAQ options above.")
                    .sessionId(session.getId())
                    .senderType("SYSTEM")
                    .status("OPEN")
                    .timestamp(LocalDateTime.now())
                    .build();

            messagingTemplate.convertAndSend(
                "/topic/session/"
                + session.getId(), blocked);
            return blocked;
        }

        // Save user message
        saveMessage(session.getId(), "USER",
            request.getMessage(), null, null,
            "FREE_CHAT");

        // Detect intent — Phase 1: keyword match
        // Phase 2: replace with LLM call here
        Optional<CbFaq> faqOpt = intentDetector
            .detect(request.getMessage(),
                    request.getAppId());

        String reply;
        String intent = "fallback";

        if (faqOpt.isPresent()) {
            CbFaq faq = faqOpt.get();
            intent = faq.getIntent();

            if (Boolean.TRUE.equals(faq.getNeedsDb())) {
                reply = resolveDynamicAnswer(
                    faq.getIntent(),
                    request.getUserId(),
                    request.getAppId(),
                    session.getContextOrderId());
            } else {
                reply = faq.getAnswer();
            }

            saveMessage(session.getId(), "BOT",
                reply, intent, faq.getId(),
                "FREE_CHAT");
        } else {
            // No match — raise ticket
            reply = raiseTicketAndGetReply(
                session, request.getUserId(),
                request.getAppId(),
                request.getMessage());
            saveMessage(session.getId(), "BOT",
                reply, "fallback", null,
                "FREE_CHAT");
        }

        WebSocketChatResponse response =
            WebSocketChatResponse.builder()
                .reply(reply)
                .intent(intent)
                .sessionId(session.getId())
                .senderType("BOT")
                .status("OPEN")
                .timestamp(LocalDateTime.now())
                .build();

        // Broadcast to WebSocket topic
        messagingTemplate.convertAndSend(
            "/topic/session/"
            + session.getId(), response);

        return response;
    }

    // ─────────────────────────────────────────────
    // END SESSION (from free chat)
    // ─────────────────────────────────────────────
    @Override
    public SessionStatusResponse endSession(
            UUID sessionId,
            String externalUserId,
            String appId) {

        CbChatSession session = sessionRepo
            .findById(sessionId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Session not found"));

        session.setStatus("RESOLVED");
        session.setResolutionType("RESOLVED");
        session.setChatEnabled(false);
        session.setEndedAt(LocalDateTime.now());
        sessionRepo.save(session);

        saveMessage(session.getId(), "SYSTEM",
            "✅ Chat ended. Thank you for contacting support!",
            null, null, "SYSTEM");

        messagingTemplate.convertAndSend(
            "/topic/session/" + sessionId,
            WebSocketChatResponse.builder()
                .reply("Chat ended. Thank you!")
                .sessionId(sessionId)
                .senderType("SYSTEM")
                .status("RESOLVED")
                .timestamp(LocalDateTime.now())
                .build());

        log.info("Session ended → {}", sessionId);

        return SessionStatusResponse.builder()
            .sessionId(sessionId)
            .status("RESOLVED")
            .chatEnabled(false)
            .resolutionType("RESOLVED")
            .message("Chat ended successfully.")
            .timestamp(LocalDateTime.now())
            .build();
    }

    // ─────────────────────────────────────────────
    // GET HISTORY — single session
    // ─────────────────────────────────────────────
    @Override
    public ChatHistoryResponse getSessionHistory(
            UUID sessionId) {

        CbChatSession session = sessionRepo
            .findById(sessionId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Session not found"));

        List<CbChatMessage> messages =
            messageRepo
                .findBySessionIdOrderBySentAtAsc(
                    sessionId);

        return buildHistoryResponse(session, messages);
    }

    // ─────────────────────────────────────────────
    // GET ALL HISTORY — all sessions for this user
    // ─────────────────────────────────────────────
    @Override
    public List<ChatHistoryResponse> getAllHistory(
            String externalUserId, String appId) {

        CbUser user = findUser(externalUserId, appId);

        List<CbChatSession> sessions =
            sessionRepo.findAllByUserId(user.getId());

        return sessions.stream()
            .map(session -> {
                List<CbChatMessage> messages =
                    messageRepo
                        .findBySessionIdOrderBySentAtAsc(
                            session.getId());
                return buildHistoryResponse(
                    session, messages);
            })
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────
    private ChatHistoryResponse buildHistoryResponse(
            CbChatSession session,
            List<CbChatMessage> messages) {

        List<ChatHistoryResponse.MessageDto> msgDtos =
            messages.stream()
                .map(m -> ChatHistoryResponse
                    .MessageDto.builder()
                    .id(m.getId())
                    .senderType(m.getSenderType())
                    .message(m.getMessage())
                    .messageType(m.getMessageType())
                    .intent(m.getIntent())
                    .sentAt(m.getSentAt())
                    .build())
                .collect(Collectors.toList());

        return ChatHistoryResponse.builder()
            .sessionId(session.getId())
            .appId(session.getAppId())
            .status(session.getStatus())
            .resolutionType(session.getResolutionType())
            .chatEnabled(session.getChatEnabled())
            .contextOrderId(session.getContextOrderId())
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .messages(msgDtos)
            .build();
    }

    private String resolveDynamicAnswer(
            String intent,
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        return switch (intent) {
            case "track_order",
                 "order_status",
                 "delivery_delay",
                 "order_not_delivered",
                 "new_order",
                 "order_preparation_time",
                 "customer_cancelled",
                 "rider_not_arrived",
                 "current_order",
                 "order_picked_status",
                 "delivery_completed" ->
                orderQueryService.getOrderStatus(
                    externalUserId, appId,
                    contextOrderId);

            case "order_history",
                 "vendor_order_history" ->
                orderQueryService.getOrderHistory(
                    externalUserId, appId);

            case "order_timeline" ->
                orderQueryService.getOrderTimeline(
                    externalUserId, appId,
                    contextOrderId);

            case "cancel_order",
                 "vendor_cancel_order" ->
                orderQueryService.checkCancelEligibility(
                    externalUserId, appId,
                    contextOrderId);

            case "refund_status" ->
                paymentQueryService.getRefundStatus(
                    externalUserId, appId,
                    contextOrderId);

            case "money_deducted",
                 "payment_failed" ->
                paymentQueryService
                    .getPaymentFailureStatus(
                        externalUserId, appId,
                        contextOrderId);

            case "payout_status",
                 "payout_not_received",
                 "payout_rider",
                 "payment_not_received_rider",
                 "earnings_today" ->
                paymentQueryService
                    .getLatestPaymentStatus(
                        externalUserId, appId,
                        contextOrderId);

            default -> "I am checking this. Please hold.";
        };
    }

    private String raiseTicketAndGetReply(
            CbChatSession session,
            String externalUserId,
            String appId,
            String message) {

        CbUser user = findUser(externalUserId, appId);

        CbTicket ticket = CbTicket.builder()
            .sessionId(session.getId())
            .cbUserId(user.getId())
            .cbOrderId(session.getContextOrderId())
            .appId(appId)
            .category("GENERAL")
            .description(message)
            .status("OPEN")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        ticketRepo.save(ticket);

        return "🎫 Support ticket created.\n"
             + "Our team will contact you within "
             + getSlaTime(appId) + ".\n"
             + "Anything else I can help you with?";
    }

    private void saveMessage(
            UUID sessionId,
            String senderType,
            String message,
            String intent,
            UUID faqId,
            String messageType) {

        messageRepo.save(CbChatMessage.builder()
            .sessionId(sessionId)
            .senderType(senderType)
            .message(message)
            .intent(intent)
            .matchedFaqId(faqId)
            .messageType(messageType)
            .sentAt(LocalDateTime.now())
            .build());
    }

    private CbUser findUser(
            String externalUserId, String appId) {
        return userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"));
    }

    private String buildWelcomeMessage(
            String appId, CbOrder order) {

        if (order != null) {
            return switch (appId) {
                case "USER" ->
                    "👋 Hi! I can see your order from "
                  + order.getRestaurantName()
                  + " worth ₹"
                  + order.getTotalAmount()
                  + "\nStatus: "
                  + order.getOrderStatus()
                  + "\nPlease select a category below.";
                case "VENDOR" ->
                    "👋 Hi! Order "
                  + order.getExternalOrderId()
                  + " | Status: "
                  + order.getOrderStatus()
                  + "\nPlease select a category below.";
                case "RIDER" ->
                    "👋 Hi! Delivery "
                  + order.getExternalOrderId()
                  + " | Status: "
                  + order.getOrderStatus()
                  + "\nPlease select a category below.";
                default ->
                    "👋 Hi! How can I help you today?";
            };
        }

        return switch (appId) {
            case "USER" ->
                "👋 Hi! I am your support assistant.\n"
              + "Please select a category below.";
            case "VENDOR" ->
                "👋 Hi! I am your vendor support assistant.\n"
              + "Please select a category below.";
            case "RIDER" ->
                "👋 Hi! I am your delivery support assistant.\n"
              + "Please select a category below.";
            default ->
                "👋 Hi! How can I help you?";
        };
    }

    private String getSlaTime(String appId) {
        return switch (appId) {
            case "RIDER"  -> "1 hour";
            case "VENDOR" -> "4 hours";
            default       -> "2 hours";
        };
    }
}