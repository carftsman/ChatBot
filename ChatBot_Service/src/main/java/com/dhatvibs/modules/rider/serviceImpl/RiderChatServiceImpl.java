package com.dhatvibs.modules.rider.serviceImpl;


import com.dhatvibs.modules.rider.dto.*;
import com.dhatvibs.modules.rider.entities.*;
import com.dhatvibs.modules.rider.repository.*;
import com.dhatvibs.modules.config.chat.ChatSessionAccess;
import com.dhatvibs.modules.rider.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderChatServiceImpl
        implements RiderChatService {

    private final RiderChatSessionRepository sessionRepo;
    private final RiderChatMessageRepository messageRepo;
    private final RiderTicketRepository      ticketRepo;
    private final RiderQueryService          queryService;
    private final RiderLLMService            llmService;
    private final SimpMessagingTemplate      messaging;

	/*
	 * @Override public ChatStartResponse startSession( String riderId, String
	 * riderToken) {
	 * 
	 * RiderChatSession session = RiderChatSession.builder() .riderId(riderId)
	 * .riderToken(riderToken) .status("OPEN") .chatEnabled(false)
	 * .startedAt(LocalDateTime.now()) .build();
	 * 
	 * session = sessionRepo.save(session);
	 * 
	 * String welcome = "Hi! I am your support assistant.\n" +
	 * "Please select a category below.";
	 * 
	 * saveMessage(session.getId(), "SYSTEM", welcome, null, null, "SYSTEM");
	 * 
	 * log.info("Rider session started: {}", session.getId());
	 * 
	 * return ChatStartResponse.builder() .sessionId(session.getId())
	 * .status("OPEN") .welcomeMessage(welcome) .chatEnabled(false)
	 * .startedAt(session.getStartedAt()) .build(); }
	 */

	/*
	 * @Override public SessionStatusResponse resolveOrEscalate( UUID sessionId,
	 * boolean resolved, String riderId) {
	 * 
	 * RiderChatSession session = sessionRepo.findById(sessionId) .orElseThrow(() ->
	 * new ResponseStatusException( HttpStatus.NOT_FOUND, "Session not found"));
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
	 * return SessionStatusResponse.builder() .sessionId(sessionId)
	 * .status("RESOLVED") .chatEnabled(false) .resolutionType("RESOLVED")
	 * .message("Issue resolved.") .timestamp(LocalDateTime.now()) .build();
	 * 
	 * } else { session.setChatEnabled(true);
	 * session.setResolutionType("ESCALATED"); sessionRepo.save(session);
	 * 
	 * String msg = "Free chat enabled. " + "Please describe your issue.";
	 * saveMessage(sessionId, "SYSTEM", msg, null, null, "SYSTEM");
	 * broadcast(sessionId, msg, "SYSTEM", "ESCALATED");
	 * 
	 * return SessionStatusResponse.builder() .sessionId(sessionId) .status("OPEN")
	 * .chatEnabled(true) .resolutionType("ESCALATED")
	 * .message("Free chat enabled.") .timestamp(LocalDateTime.now()) .build(); } }
	 */
    
    
    @Override
    public ChatStartResponse startSession(
            String riderId,
            String riderToken,
            String orderId) {

        log.info("=== startSession called ===");
        log.info("riderId:    {}", riderId);
        log.info("orderId:    {}", orderId);
        log.info("riderToken: {}",
                 riderToken != null
                     ? riderToken.substring(0,
                         Math.min(20,
                             riderToken.length()))
                     : "NULL");

        RiderChatSession session =
            RiderChatSession.builder()
                .riderId(riderId)
                .riderToken(riderToken)
                .contextOrderId(orderId)
                .status("OPEN")
                .chatEnabled(false)
                .startedAt(LocalDateTime.now())
                .build();

        log.info("Before save → contextOrderId: {}",
                 session.getContextOrderId());

        session = sessionRepo.save(session);

        log.info("After save → sessionId: {} | "
               + "contextOrderId: {}",
                 session.getId(),
                 session.getContextOrderId());

        String welcome = (orderId != null
                && !orderId.isBlank())
            ? "Hi! I can see you selected order "
              + orderId + ".\n"
              + "Please select a category below."
            : "Hi! I am your support assistant.\n"
              + "Please select a category below.";

        saveMessage(session.getId(), "SYSTEM",
            welcome, null, null, "SYSTEM");

        return ChatStartResponse.builder()
            .sessionId(session.getId())
            .status("OPEN")
            .welcomeMessage(welcome)
            .chatEnabled(false)
            .startedAt(session.getStartedAt())
            .build();
    }
    
    @Override
    public SessionStatusResponse resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String riderId) {

        RiderChatSession session =
            sessionRepo.findById(sessionId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        ChatSessionAccess.assertOwner(
            session.getRiderId(),
            riderId);

        if (resolved) {
            //  Issue Resolved — close session
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

            return SessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("RESOLVED")
                .chatEnabled(false)
                .resolutionType("RESOLVED")
                .message("Issue resolved.")
                .timestamp(LocalDateTime.now())
                .build();

        } else {
            //  Issue Not Resolved
            // Enable chat AND raise ticket immediately
            session.setChatEnabled(true);
            session.setResolutionType("ESCALATED");
            sessionRepo.save(session);

            // ── Raise ticket immediately ──────────────
            RiderTicket ticket = RiderTicket.builder()
                .sessionId(sessionId)
                .riderId(session.getRiderId())
                .category("SUPPORT")
                .description(
                    "Rider clicked Issue Not Resolved. "
                  + "Free chat enabled for follow up.")
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

            RiderTicket saved = ticketRepo.save(ticket);

            log.info("Ticket raised on escalation: {}",
                     saved.getId());

            String msg = "Free chat is now enabled.\n"
                + "Support ticket #"
                + saved.getId().toString()
                       .substring(0, 8).toUpperCase()
                + " has been created.\n"
                + "Please describe your issue.";

            saveMessage(sessionId, "SYSTEM",
                msg, null, null, "SYSTEM");

            broadcast(sessionId, msg,
                "SYSTEM", "ESCALATED");

            return SessionStatusResponse.builder()
                .sessionId(sessionId)
                .status("OPEN")
                .chatEnabled(true)
                .resolutionType("ESCALATED")
                .message("Free chat enabled. "
                       + "Ticket raised.")
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    @Override
    public WebSocketChatResponse processMessage(
            WebSocketChatRequest request) {

        RiderChatSession session =
            sessionRepo.findById(
                    request.getSessionId())
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        if (request.getRiderId() != null) {
            ChatSessionAccess.assertOwner(
                session.getRiderId(),
                request.getRiderId());
        }

        if (!Boolean.TRUE.equals(
                session.getChatEnabled())) {
            WebSocketChatResponse blocked =
                WebSocketChatResponse.builder()
                    .reply("Please use FAQ options.")
                    .sessionId(session.getId())
                    .senderType("SYSTEM")
                    .status("OPEN")
                    .timestamp(LocalDateTime.now())
                    .build();
            messaging.convertAndSend(
                "/topic/rider/"
                + session.getId(), blocked);
            return blocked;
        }

        // Save user message
        saveMessage(session.getId(), "USER",
            request.getMessage(),
            null, null, "FREE_CHAT");

        // Fetch last 5 messages for LLM memory
        List<RiderChatMessage> all =
            messageRepo
                .findBySessionIdOrderBySentAtAsc(
                    session.getId());

        int total = all.size();
        int from  = Math.max(0, total - 6);
        List<RiderChatMessage> last5 = total > 1
            ? all.subList(from, total - 1)
            : Collections.emptyList();

        List<RiderLLMService.ConversationMessage>
            history = last5.stream()
                .filter(m -> m.getMessage() != null
                    && List.of("USER","BOT")
                           .contains(
                               m.getSenderType()))
                .map(m ->
                    new RiderLLMService
                        .ConversationMessage(
                        m.getSenderType(),
                        m.getMessage()))
                .collect(Collectors.toList());

        // LLM classify
        RiderLLMService.LLMResult llmResult =
            llmService.processMessage(
                request.getMessage(),
                getRiderName(session.getRiderToken()),
                history);

        log.info("LLM intent: {} | needsApi: {}",
                 llmResult.intent(),
                 llmResult.needsApi());

        String reply;
        String intent = llmResult.intent();

        if (!llmResult.needsApi()) {
            reply = llmResult.directAnswer() != null
                ? llmResult.directAnswer()
                : "I am here to help! "
                + "What can I assist you with?";
        } else {
            reply = resolveFromApi(
                intent, session.getRiderToken());

            if (reply == null || reply.isBlank()) {
                reply = raiseTicketAndGetReply(
                    session,
                    request.getMessage());
                intent = "fallback";
            }
        }

        saveMessage(session.getId(), "BOT",
            reply, intent, null, "FREE_CHAT");

        WebSocketChatResponse response =
            WebSocketChatResponse.builder()
                .reply(reply)
                .intent(intent)
                .sessionId(session.getId())
                .senderType("BOT")
                .status("OPEN")
                .timestamp(LocalDateTime.now())
                .build();

        messaging.convertAndSend(
            "/topic/rider/"
            + session.getId(), response);

        return response;
    }

    @Override
    public SessionStatusResponse endSession(
            UUID sessionId) {

        RiderChatSession session =
            sessionRepo.findById(sessionId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        session.setStatus("RESOLVED");
        session.setResolutionType("RESOLVED");
        session.setChatEnabled(false);
        session.setEndedAt(LocalDateTime.now());
        sessionRepo.save(session);

        saveMessage(sessionId, "SYSTEM",
            "Chat ended. Thank you!",
            null, null, "SYSTEM");

        broadcast(sessionId,
            "Chat ended. Thank you!",
            "SYSTEM", "RESOLVED");

        return SessionStatusResponse.builder()
            .sessionId(sessionId)
            .status("RESOLVED")
            .chatEnabled(false)
            .message("Chat ended.")
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public ChatHistoryResponse getSessionHistory(
            UUID sessionId) {

        RiderChatSession session =
            sessionRepo.findById(sessionId)
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        List<RiderChatMessage> messages =
            messageRepo
                .findBySessionIdOrderBySentAtAsc(
                    sessionId);

        return buildHistory(session, messages);
    }

    @Override
    public List<ChatHistoryResponse> getAllHistory(
            String riderId) {

        return sessionRepo
            .findAllByRiderId(riderId)
            .stream()
            .map(session -> buildHistory(
                session,
                messageRepo
                    .findBySessionIdOrderBySentAtAsc(
                        session.getId())))
            .collect(Collectors.toList());
    }

    // ── PRIVATE HELPERS ───────────────────────────

    private String resolveFromApi(
            String intent, String token) {
        if (token == null)
            return "Session expired. Login again.";

        return switch (intent) {
            case "earnings_today"    ->
                queryService.getDailyEarnings(token);
            case "earnings_weekly"   ->
                queryService.getWeeklyEarnings(token);
            case "earnings_summary",
                 "payout_status",
                 "payout_rider"      ->
                queryService.getEarningsSummary(token);
            case "cash_balance"      ->
                queryService.getCashBalance(token);
            case "order_stats",
                 "current_order",
                 "order_history"     ->
                queryService.getOrderHistory(token);
            case "ratings"           ->
                queryService.getRatings(token);
            case "weekly_performance"->
                queryService.getWeeklyPerformance(
                    token);
            case "rider_profile"     ->
                queryService.getRiderProfile(token);
            case "wallet_balance"    ->
                queryService.getWalletBalance(token);
            default ->
                null;
        };
    }

    private String raiseTicketAndGetReply(
            RiderChatSession session,
            String message) {

        RiderTicket ticket = RiderTicket.builder()
            .sessionId(session.getId())
            .riderId(session.getRiderId())
            .category("GENERAL")
            .description(message)
            .status("OPEN")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        ticketRepo.save(ticket);

        return "Support ticket created.\n"
             + "Our team will contact you "
             + "within 1 hour.";
    }

    private void saveMessage(
            UUID sessionId, String senderType,
            String message, String intent,
            UUID faqId, String messageType) {
        messageRepo.save(
            RiderChatMessage.builder()
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
            "/topic/rider/" + sessionId,
            WebSocketChatResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .senderType(senderType)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build());
    }

    private String getRiderName(String token) {
        return "Rider";
    }

    private ChatHistoryResponse buildHistory(
            RiderChatSession session,
            List<RiderChatMessage> messages) {

        List<ChatHistoryResponse.MessageDto> dtos =
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
            .status(session.getStatus())
            .resolutionType(
                session.getResolutionType())
            .chatEnabled(session.getChatEnabled())
            .startedAt(session.getStartedAt())
            .endedAt(session.getEndedAt())
            .messages(dtos)
            .build();
    }
    
    
 // Add to RiderChatServiceImpl.java
    @Override
    public ChatHistoryResponse getHistoryByOrderId(
            String orderId,
            String riderId) {

        RiderChatSession session = sessionRepo
            .findByContextOrderId(orderId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No chat found for order: "
                    + orderId));

        ChatSessionAccess.assertOwner(
            session.getRiderId(),
            riderId);

        List<RiderChatMessage> messages =
            messageRepo
                .findBySessionIdOrderBySentAtAsc(
                    session.getId());

        return buildHistory(session, messages);
    }
}