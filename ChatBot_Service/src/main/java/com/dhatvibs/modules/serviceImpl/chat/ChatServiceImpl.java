/*
 * package com.dhatvibs.modules.serviceImpl.chat;
 * 
 * 
 * 
 * import lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j;
 * import org.springframework.http.HttpStatus; import
 * org.springframework.messaging.simp.SimpMessagingTemplate; import
 * org.springframework.stereotype.Service; import
 * org.springframework.web.server.ResponseStatusException;
 * 
 * import com.dhatvibs.modules.dto.chat.*; import
 * com.dhatvibs.modules.entities.auth.CbUser; import
 * com.dhatvibs.modules.entities.chat.CbChatMessage; import
 * com.dhatvibs.modules.entities.chat.CbChatSession; import
 * com.dhatvibs.modules.entities.chat.CbFaq; import
 * com.dhatvibs.modules.entities.chat.CbOrder; import
 * com.dhatvibs.modules.entities.chat.CbTicket; import
 * com.dhatvibs.modules.repository.auth.CbUserRepository; import
 * com.dhatvibs.modules.repository.chat.*; import
 * com.dhatvibs.modules.service.chat.*;
 * 
 * import java.time.LocalDateTime; import java.util.*; import
 * java.util.stream.Collectors;
 * 
 * @Slf4j
 * 
 * @Service
 * 
 * @RequiredArgsConstructor public class ChatServiceImpl implements ChatService
 * {
 * 
 * private final CbChatSessionRepository sessionRepo; private final
 * CbChatMessageRepository messageRepo; private final CbTicketRepository
 * ticketRepo; private final CbUserRepository userRepo; private final
 * CbOrderRepository orderRepo; private final IntentDetectorService
 * intentDetector; private final OrderQueryService orderQueryService; private
 * final PaymentQueryService paymentQueryService; private final
 * SimpMessagingTemplate messagingTemplate;
 * 
 * 
 * private final LLMService llmService;
 * 
 * // ───────────────────────────────────────────── // START SESSION //
 * chatEnabled = FALSE by default //
 * ─────────────────────────────────────────────
 * 
 * @Override public ChatStartResponse startSession( String externalUserId,
 * String appId, UUID orderId) {
 * 
 * CbUser user = findUser(externalUserId, appId);
 * 
 * CbOrder order = null; if (orderId != null) { order =
 * orderRepo.findById(orderId) .orElseThrow(() -> new ResponseStatusException(
 * HttpStatus.NOT_FOUND, "Order not found")); }
 * 
 * // Create session — chat NOT enabled yet CbChatSession session =
 * CbChatSession.builder() .cbUserId(user.getId()) .appId(appId) .status("OPEN")
 * .contextOrderId(orderId) .chatEnabled(false) // FAQ only initially
 * .startedAt(LocalDateTime.now()) .build();
 * 
 * session = sessionRepo.save(session);
 * 
 * // Save system welcome message String welcome = buildWelcomeMessage( appId,
 * order); saveMessage(session.getId(), "SYSTEM", welcome, null, null,
 * "SYSTEM");
 * 
 * log.info("Session started → {} | chatEnabled: false", session.getId());
 * 
 * return ChatStartResponse.builder() .sessionId(session.getId()) .appId(appId)
 * .status("OPEN") .welcomeMessage(welcome) .contextOrderId(orderId)
 * .startedAt(session.getStartedAt()) .build(); }
 * 
 * // ───────────────────────────────────────────── // RESOLVE OR ESCALATE //
 * Called when user taps: // ✅ "Issue Resolved" → resolved = true // ❌
 * "Issue Not Resolved" → resolved = false //
 * ─────────────────────────────────────────────
 * 
 * @Override public SessionStatusResponse resolveOrEscalate( UUID sessionId,
 * boolean resolved, String externalUserId, String appId) {
 * 
 * CbChatSession session = sessionRepo .findById(sessionId) .orElseThrow(() ->
 * new ResponseStatusException( HttpStatus.NOT_FOUND, "Session not found"));
 * 
 * if (resolved) { // ✅ User happy — close session
 * session.setStatus("RESOLVED"); session.setResolutionType("RESOLVED");
 * session.setChatEnabled(false); session.setEndedAt(LocalDateTime.now());
 * sessionRepo.save(session);
 * 
 * // Save system message saveMessage(session.getId(), "SYSTEM",
 * "✅ Your issue has been marked as resolved. " +
 * "Thank you for contacting support!", null, null, "SYSTEM");
 * 
 * // Broadcast session closed via WebSocket messagingTemplate.convertAndSend(
 * "/topic/session/" + sessionId, WebSocketChatResponse.builder()
 * .reply("Session closed. Thank you!") .sessionId(sessionId)
 * .senderType("SYSTEM") .status("RESOLVED") .timestamp(LocalDateTime.now())
 * .build());
 * 
 * log.info("Session RESOLVED → {}", sessionId);
 * 
 * return SessionStatusResponse.builder() .sessionId(sessionId)
 * .status("RESOLVED") .chatEnabled(false) .resolutionType("RESOLVED")
 * .message("Issue resolved. Session closed.") .timestamp(LocalDateTime.now())
 * .build();
 * 
 * } else { // ❌ User not happy — enable free chat session.setChatEnabled(true);
 * session.setResolutionType("ESCALATED"); sessionRepo.save(session);
 * 
 * // Save system message enabling chat String escalateMsg =
 * "💬 Free chat is now enabled.\n" + "Please describe your issue in detail " +
 * "and our support will assist you.";
 * 
 * saveMessage(session.getId(), "SYSTEM", escalateMsg, null, null, "SYSTEM");
 * 
 * // Broadcast chat enabled via WebSocket messagingTemplate.convertAndSend(
 * "/topic/session/" + sessionId, WebSocketChatResponse.builder()
 * .reply(escalateMsg) .sessionId(sessionId) .senderType("SYSTEM")
 * .status("ESCALATED") .timestamp(LocalDateTime.now()) .build());
 * 
 * log.info("Chat ENABLED → {}", sessionId);
 * 
 * return SessionStatusResponse.builder() .sessionId(sessionId) .status("OPEN")
 * .chatEnabled(true) .resolutionType("ESCALATED")
 * .message("Free chat enabled. " + "You can now type your message.")
 * .timestamp(LocalDateTime.now()) .build(); } }
 * 
 * // ───────────────────────────────────────────── // PROCESS FREE CHAT MESSAGE
 * (WebSocket) // Only works when chatEnabled = TRUE //
 * ─────────────────────────────────────────────
 * 
 * 
 * @Override public WebSocketChatResponse processMessage( WebSocketChatRequest
 * request) {
 * 
 * CbChatSession session = sessionRepo .findById(request.getSessionId())
 * .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND,
 * "Session not found"));
 * 
 * // Guard — chat must be enabled if (!Boolean.TRUE.equals(
 * session.getChatEnabled())) { WebSocketChatResponse blocked =
 * WebSocketChatResponse.builder() .reply("❌ Free chat is not enabled yet. " +
 * "Please use the FAQ options above.") .sessionId(session.getId())
 * .senderType("SYSTEM") .status("OPEN") .timestamp(LocalDateTime.now())
 * .build();
 * 
 * messagingTemplate.convertAndSend( "/topic/session/" + session.getId(),
 * blocked); return blocked; }
 * 
 * // Save user message saveMessage(session.getId(), "USER",
 * request.getMessage(), null, null, "FREE_CHAT");
 * 
 * // Detect intent — Phase 1: keyword match // Phase 2: replace with LLM call
 * here Optional<CbFaq> faqOpt = intentDetector .detect(request.getMessage(),
 * request.getAppId());
 * 
 * String reply; String intent = "fallback";
 * 
 * if (faqOpt.isPresent()) { CbFaq faq = faqOpt.get(); intent = faq.getIntent();
 * 
 * if (Boolean.TRUE.equals(faq.getNeedsDb())) { reply = resolveDynamicAnswer(
 * faq.getIntent(), request.getUserId(), request.getAppId(),
 * session.getContextOrderId()); } else { reply = faq.getAnswer(); }
 * 
 * saveMessage(session.getId(), "BOT", reply, intent, faq.getId(), "FREE_CHAT");
 * } else { // No match — raise ticket reply = raiseTicketAndGetReply( session,
 * request.getUserId(), request.getAppId(), request.getMessage());
 * saveMessage(session.getId(), "BOT", reply, "fallback", null, "FREE_CHAT"); }
 * 
 * WebSocketChatResponse response = WebSocketChatResponse.builder()
 * .reply(reply) .intent(intent) .sessionId(session.getId()) .senderType("BOT")
 * .status("OPEN") .timestamp(LocalDateTime.now()) .build();
 * 
 * // Broadcast to WebSocket topic messagingTemplate.convertAndSend(
 * "/topic/session/" + session.getId(), response);
 * 
 * return response; }
 * 
 * 
 * 
 * // Replace the processMessage method:
 * 
 * @Override public WebSocketChatResponse processMessage( WebSocketChatRequest
 * request) {
 * 
 * CbChatSession session = sessionRepo .findById(request.getSessionId())
 * .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND,
 * "Session not found"));
 * 
 * if (!Boolean.TRUE.equals(session.getChatEnabled())) { WebSocketChatResponse
 * blocked = WebSocketChatResponse.builder()
 * .reply("Please use the FAQ options above.") .sessionId(session.getId())
 * .senderType("SYSTEM") .status("OPEN") .timestamp(LocalDateTime.now())
 * .build(); messagingTemplate.convertAndSend( "/topic/session/" +
 * session.getId(), blocked); return blocked; }
 * 
 * // Save user message saveMessage(session.getId(), "USER",
 * request.getMessage(), null, null, "FREE_CHAT");
 * 
 * // ── PHASE 2: LLM replaces keyword matching ── LLMService.LLMResult
 * llmResult = llmService.processMessage( request.getMessage(),
 * getUserName(request.getUserId(), request.getAppId()), request.getAppId());
 * 
 * log.info("=== LLM RESULT ==="); log.info("Message:  {}",
 * request.getMessage()); log.info("Intent:   {}", llmResult.intent());
 * log.info("NeedsDB:  {}", llmResult.needsDB()); log.info("Answer:   {}",
 * llmResult.directAnswer()); log.info("==================");
 * 
 * String reply; String intent = llmResult.intent();
 * 
 * if (!llmResult.needsDB()) { // LLM handles directly — greeting, thanks etc.
 * reply = llmResult.directAnswer() != null ? llmResult.directAnswer() :
 * "I am here to help! What can I assist you with?";
 * 
 * } else { // Known intent — query DB reply = resolveDynamicAnswer( intent,
 * request.getUserId(), request.getAppId(), session.getContextOrderId());
 * 
 * // If DB returned nothing useful — raise ticket if (reply == null ||
 * reply.isBlank()) { reply = raiseTicketAndGetReply( session,
 * request.getUserId(), request.getAppId(), request.getMessage()); intent =
 * "fallback"; } }
 * 
 * saveMessage(session.getId(), "BOT", reply, intent, null, "FREE_CHAT");
 * 
 * WebSocketChatResponse response = WebSocketChatResponse.builder()
 * .reply(reply) .intent(intent) .sessionId(session.getId()) .senderType("BOT")
 * .status("OPEN") .timestamp(LocalDateTime.now()) .build();
 * 
 * messagingTemplate.convertAndSend( "/topic/session/" + session.getId(),
 * response);
 * 
 * return response; }
 * 
 * // Helper — get user name for LLM prompt private String getUserName( String
 * externalUserId, String appId) { return userRepo
 * .findByExternalUserIdAndAppId( externalUserId, appId) .map(u ->
 * u.getFullName() != null ? u.getFullName() : "there") .orElse("there"); }
 * 
 * 
 * 
 * 
 * // ───────────────────────────────────────────── // END SESSION (from free
 * chat) // ─────────────────────────────────────────────
 * 
 * @Override public SessionStatusResponse endSession( UUID sessionId, String
 * externalUserId, String appId) {
 * 
 * CbChatSession session = sessionRepo .findById(sessionId) .orElseThrow(() ->
 * new ResponseStatusException( HttpStatus.NOT_FOUND, "Session not found"));
 * 
 * session.setStatus("RESOLVED"); session.setResolutionType("RESOLVED");
 * session.setChatEnabled(false); session.setEndedAt(LocalDateTime.now());
 * sessionRepo.save(session);
 * 
 * saveMessage(session.getId(), "SYSTEM",
 * "✅ Chat ended. Thank you for contacting support!", null, null, "SYSTEM");
 * 
 * messagingTemplate.convertAndSend( "/topic/session/" + sessionId,
 * WebSocketChatResponse.builder() .reply("Chat ended. Thank you!")
 * .sessionId(sessionId) .senderType("SYSTEM") .status("RESOLVED")
 * .timestamp(LocalDateTime.now()) .build());
 * 
 * log.info("Session ended → {}", sessionId);
 * 
 * return SessionStatusResponse.builder() .sessionId(sessionId)
 * .status("RESOLVED") .chatEnabled(false) .resolutionType("RESOLVED")
 * .message("Chat ended successfully.") .timestamp(LocalDateTime.now())
 * .build(); }
 * 
 * // ───────────────────────────────────────────── // GET HISTORY — single
 * session // ─────────────────────────────────────────────
 * 
 * @Override public ChatHistoryResponse getSessionHistory( UUID sessionId) {
 * 
 * CbChatSession session = sessionRepo .findById(sessionId) .orElseThrow(() ->
 * new ResponseStatusException( HttpStatus.NOT_FOUND, "Session not found"));
 * 
 * List<CbChatMessage> messages = messageRepo .findBySessionIdOrderBySentAtAsc(
 * sessionId);
 * 
 * return buildHistoryResponse(session, messages); }
 * 
 * // ───────────────────────────────────────────── // GET ALL HISTORY — all
 * sessions for this user // ─────────────────────────────────────────────
 * 
 * @Override public List<ChatHistoryResponse> getAllHistory( String
 * externalUserId, String appId) {
 * 
 * CbUser user = findUser(externalUserId, appId);
 * 
 * List<CbChatSession> sessions = sessionRepo.findAllByUserId(user.getId());
 * 
 * return sessions.stream() .map(session -> { List<CbChatMessage> messages =
 * messageRepo .findBySessionIdOrderBySentAtAsc( session.getId()); return
 * buildHistoryResponse( session, messages); }) .collect(Collectors.toList()); }
 * 
 * // ───────────────────────────────────────────── // PRIVATE HELPERS //
 * ───────────────────────────────────────────── private ChatHistoryResponse
 * buildHistoryResponse( CbChatSession session, List<CbChatMessage> messages) {
 * 
 * List<ChatHistoryResponse.MessageDto> msgDtos = messages.stream() .map(m ->
 * ChatHistoryResponse .MessageDto.builder() .id(m.getId())
 * .senderType(m.getSenderType()) .message(m.getMessage())
 * .messageType(m.getMessageType()) .intent(m.getIntent())
 * .sentAt(m.getSentAt()) .build()) .collect(Collectors.toList());
 * 
 * return ChatHistoryResponse.builder() .sessionId(session.getId())
 * .appId(session.getAppId()) .status(session.getStatus())
 * .resolutionType(session.getResolutionType())
 * .chatEnabled(session.getChatEnabled())
 * .contextOrderId(session.getContextOrderId())
 * .startedAt(session.getStartedAt()) .endedAt(session.getEndedAt())
 * .messages(msgDtos) .build(); }
 * 
 * private String resolveDynamicAnswer( String intent, String externalUserId,
 * String appId, UUID contextOrderId) {
 * 
 * return switch (intent) { case "track_order", "order_status",
 * "delivery_delay", "order_not_delivered", "new_order",
 * "order_preparation_time", "customer_cancelled", "rider_not_arrived",
 * "current_order", "order_picked_status", "delivery_completed" ->
 * orderQueryService.getOrderStatus( externalUserId, appId, contextOrderId);
 * 
 * case "order_history", "vendor_order_history" ->
 * orderQueryService.getOrderHistory( externalUserId, appId);
 * 
 * case "order_timeline" -> orderQueryService.getOrderTimeline( externalUserId,
 * appId, contextOrderId);
 * 
 * case "cancel_order", "vendor_cancel_order" ->
 * orderQueryService.checkCancelEligibility( externalUserId, appId,
 * contextOrderId);
 * 
 * case "refund_status" -> paymentQueryService.getRefundStatus( externalUserId,
 * appId, contextOrderId);
 * 
 * case "money_deducted", "payment_failed" -> paymentQueryService
 * .getPaymentFailureStatus( externalUserId, appId, contextOrderId);
 * 
 * case "payout_status", "payout_not_received", "payout_rider",
 * "payment_not_received_rider", "earnings_today" -> paymentQueryService
 * .getLatestPaymentStatus( externalUserId, appId, contextOrderId);
 * 
 * default -> "I am checking this. Please hold."; }; }
 * 
 * private String raiseTicketAndGetReply( CbChatSession session, String
 * externalUserId, String appId, String message) {
 * 
 * CbUser user = findUser(externalUserId, appId);
 * 
 * CbTicket ticket = CbTicket.builder() .sessionId(session.getId())
 * .cbUserId(user.getId()) .cbOrderId(session.getContextOrderId()) .appId(appId)
 * .category("GENERAL") .description(message) .status("OPEN")
 * .createdAt(LocalDateTime.now()) .updatedAt(LocalDateTime.now()) .build();
 * 
 * ticketRepo.save(ticket);
 * 
 * return "🎫 Support ticket created.\n" + "Our team will contact you within " +
 * getSlaTime(appId) + ".\n" + "Anything else I can help you with?"; }
 * 
 * private void saveMessage( UUID sessionId, String senderType, String message,
 * String intent, UUID faqId, String messageType) {
 * 
 * messageRepo.save(CbChatMessage.builder() .sessionId(sessionId)
 * .senderType(senderType) .message(message) .intent(intent)
 * .matchedFaqId(faqId) .messageType(messageType) .sentAt(LocalDateTime.now())
 * .build()); }
 * 
 * private CbUser findUser( String externalUserId, String appId) { return
 * userRepo .findByExternalUserIdAndAppId( externalUserId, appId)
 * .orElseThrow(() -> new ResponseStatusException( HttpStatus.NOT_FOUND,
 * "User not found")); }
 * 
 * private String buildWelcomeMessage( String appId, CbOrder order) {
 * 
 * if (order != null) { return switch (appId) { case "USER" ->
 * "👋 Hi! I can see your order from " + order.getRestaurantName() + " worth ₹"
 * + order.getTotalAmount() + "\nStatus: " + order.getOrderStatus() +
 * "\nPlease select a category below."; case "VENDOR" -> "👋 Hi! Order " +
 * order.getExternalOrderId() + " | Status: " + order.getOrderStatus() +
 * "\nPlease select a category below."; case "RIDER" -> "👋 Hi! Delivery " +
 * order.getExternalOrderId() + " | Status: " + order.getOrderStatus() +
 * "\nPlease select a category below."; default ->
 * "👋 Hi! How can I help you today?"; }; }
 * 
 * return switch (appId) { case "USER" ->
 * "👋 Hi! I am your support assistant.\n" + "Please select a category below.";
 * case "VENDOR" -> "👋 Hi! I am your vendor support assistant.\n" +
 * "Please select a category below."; case "RIDER" ->
 * "👋 Hi! I am your delivery support assistant.\n" +
 * "Please select a category below."; default -> "👋 Hi! How can I help you?";
 * }; }
 * 
 * private String getSlaTime(String appId) { return switch (appId) { case
 * "RIDER" -> "1 hour"; case "VENDOR" -> "4 hours"; default -> "2 hours"; }; } }
 */  



package com.dhatvibs.modules.serviceImpl.chat;

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
    private final LLMService               llmService;

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

        CbChatSession session = CbChatSession.builder()
            .cbUserId(user.getId())
            .appId(appId)
            .status("OPEN")
            .contextOrderId(orderId)
            .chatEnabled(false)
            .startedAt(LocalDateTime.now())
            .build();

        session = sessionRepo.save(session);

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
            session.setStatus("RESOLVED");
            session.setResolutionType("RESOLVED");
            session.setChatEnabled(false);
            session.setEndedAt(LocalDateTime.now());
            sessionRepo.save(session);

            saveMessage(session.getId(), "SYSTEM",
                "✅ Your issue has been marked as resolved. "
              + "Thank you for contacting support!",
                null, null, "SYSTEM");

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
            session.setChatEnabled(true);
            session.setResolutionType("ESCALATED");
            sessionRepo.save(session);

            String escalateMsg =
                "💬 Free chat is now enabled.\n"
              + "Please describe your issue in detail "
              + "and our support will assist you.";

            saveMessage(session.getId(), "SYSTEM",
                escalateMsg, null, null, "SYSTEM");

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
    // PROCESS FREE CHAT MESSAGE
    // Updated with conversation memory
    // ─────────────────────────────────────────────
    @Override
    public WebSocketChatResponse processMessage(
            WebSocketChatRequest request) {

        // 1. Load session
        CbChatSession session = sessionRepo
            .findById(request.getSessionId())
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Session not found"));

        // 2. Guard — chat must be enabled
        if (!Boolean.TRUE.equals(
                session.getChatEnabled())) {
            WebSocketChatResponse blocked =
                WebSocketChatResponse.builder()
                    .reply("Please use the FAQ "
                         + "options above.")
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

        // 3. Save user message first
        saveMessage(
            session.getId(), "USER",
            request.getMessage(),
            null, null, "FREE_CHAT");

        // 4. Fetch last 5 messages for memory
        List<CbChatMessage> allMessages =
            messageRepo
                .findBySessionIdOrderBySentAtAsc(
                    session.getId());

        // exclude the message just saved
        int total = allMessages.size();
        int from  = Math.max(0, total - 6);
        List<CbChatMessage> last5 =
            total > 1
                ? allMessages.subList(from, total - 1)
                : Collections.emptyList();

        // 5. Convert to ConversationMessage list
        List<LLMService.ConversationMessage> history =
            last5.stream()
                .filter(m ->
                    m.getMessage() != null
                    && !m.getMessage().isBlank()
                    && List.of("USER", "BOT")
                           .contains(
                               m.getSenderType()))
                .map(m ->
                    new LLMService.ConversationMessage(
                        m.getSenderType(),
                        m.getMessage()))
                .collect(Collectors.toList());

        log.info("Passing {} history messages to LLM",
                 history.size());

        // 6. Call LLM with conversation memory
        LLMService.LLMResult llmResult =
            llmService.processMessage(
                request.getMessage(),
                getUserName(
                    request.getUserId(),
                    request.getAppId()),
                request.getAppId(),
                history);          // ← conversation memory

        log.info("=== LLM RESULT ===");
        log.info("Message: {}", request.getMessage());
        log.info("Intent:  {}", llmResult.intent());
        log.info("NeedsDB: {}", llmResult.needsDB());
        log.info("Answer:  {}", llmResult.directAnswer());
        log.info("==================");

        // 7. Build reply
        String reply;
        String intent = llmResult.intent();

        if (!llmResult.needsDB()) {
            // LLM answers directly
            // (greetings, thanks, general chat)
            reply = llmResult.directAnswer() != null
                ? llmResult.directAnswer()
                : "I am here to help! "
                + "What can I assist you with?";

        } else {
            // Known intent → query DB
            reply = resolveDynamicAnswer(
                intent,
                request.getUserId(),
                request.getAppId(),
                session.getContextOrderId());

            // DB returned nothing → raise ticket
            if (reply == null || reply.isBlank()) {
                reply = raiseTicketAndGetReply(
                    session,
                    request.getUserId(),
                    request.getAppId(),
                    request.getMessage());
                intent = "fallback";
            }
        }

        // 8. Save bot reply
        saveMessage(
            session.getId(), "BOT",
            reply, intent, null, "FREE_CHAT");

        // 9. Build and broadcast WebSocket response
        WebSocketChatResponse response =
            WebSocketChatResponse.builder()
                .reply(reply)
                .intent(intent)
                .sessionId(session.getId())
                .senderType("BOT")
                .status("OPEN")
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend(
            "/topic/session/"
            + session.getId(), response);

        return response;
    }

    // Helper — get user first name for LLM prompt
    private String getUserName(
            String externalUserId, String appId) {
        return userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .map(u -> {
                if (u.getFullName() != null
                        && !u.getFullName().isBlank()) {
                    // Return first name only
                    return u.getFullName()
                            .split(" ")[0];
                }
                return "there";
            })
            .orElse("there");
    }

    // ─────────────────────────────────────────────
    // END SESSION
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
            "✅ Chat ended. Thank you for "
          + "contacting support!",
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
    // GET ALL HISTORY — all sessions for user
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

            default ->
                "I am checking this. Please hold.";
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