package com.dhatvibs.modules.serviceImpl.chat;



import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.dhatvibs.modules.dto.chat.ChatRequest;
import com.dhatvibs.modules.dto.chat.ChatResponse;
import com.dhatvibs.modules.dto.chat.SessionResponse;
import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbChatMessage;
import com.dhatvibs.modules.entities.chat.CbChatSession;
import com.dhatvibs.modules.entities.chat.CbFaq;
import com.dhatvibs.modules.entities.chat.CbTicket;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.*;
import com.dhatvibs.modules.service.chat.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final IntentDetectorService   intentDetector;
    private final OrderQueryService       orderQueryService;
    private final PaymentQueryService     paymentQueryService;
    private final CbChatSessionRepository sessionRepo;
    private final CbChatMessageRepository messageRepo;
    private final CbTicketRepository      ticketRepo;
    private final CbUserRepository        userRepo;

    @Override
    public SessionResponse startSession(
            String externalUserId, String appId) {

        CbUser user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElseThrow(() ->
                new RuntimeException("User not found"));

        CbChatSession session = CbChatSession.builder()
            .cbUserId(user.getId())
            .appId(appId)
            .status("OPEN")
            .startedAt(LocalDateTime.now())
            .build();

        session = sessionRepo.save(session);

        // Save welcome message from BOT
        saveMessage(session.getId(), "BOT",
            getWelcomeMessage(appId), null, null);

        return SessionResponse.builder()
            .sessionId(session.getId())
            .appId(appId)
            .status("OPEN")
            .startedAt(session.getStartedAt())
            .build();
    }

    @Override
    public ChatResponse processMessage(
            ChatRequest request,
            String externalUserId,
            String appId) {

        // Get or create session
        UUID sessionId = request.getSessionId();
        CbChatSession session;

        if (sessionId == null) {
            SessionResponse s =
                startSession(externalUserId, appId);
            sessionId = s.getSessionId();
            session = sessionRepo.findById(sessionId)
                .orElseThrow();
        } else {
            session = sessionRepo
                .findByIdAndStatus(sessionId, "OPEN")
                .orElseThrow(() ->
                    new RuntimeException(
                        "Session not found or closed"));
        }

        // Save user message
        saveMessage(sessionId, "USER",
            request.getMessage(), null, null);

        // Detect intent
        Optional<CbFaq> faqOpt =
            intentDetector.detect(
                request.getMessage(), appId);

        if (faqOpt.isEmpty()) {
            // No FAQ match — create ticket
            String reply = raiseTicket(
                session, externalUserId, appId,
                request.getMessage());
            saveMessage(sessionId, "BOT",
                reply, "fallback", null);
            return buildResponse(reply,
                "fallback", sessionId, "ESCALATED");
        }

        CbFaq faq = faqOpt.get();
        String reply;

        // If needs DB — query live data
        if (Boolean.TRUE.equals(faq.getNeedsDb())) {
            reply = resolveDynamicAnswer(
                faq.getIntent(), externalUserId, appId);
        } else {
            reply = faq.getAnswer();
        }

        // If fallback intent — auto create ticket
        if ("fallback".equals(faq.getIntent())) {
            raiseTicket(session, externalUserId,
                appId, request.getMessage());
        }

        saveMessage(sessionId, "BOT",
            reply, faq.getIntent(), faq.getId());

        return buildResponse(reply, faq.getIntent(),
            sessionId, "OPEN");
    }

    @Override
    public void endSession(UUID sessionId) {
        sessionRepo.findById(sessionId).ifPresent(s -> {
            s.setStatus("RESOLVED");
            s.setEndedAt(LocalDateTime.now());
            sessionRepo.save(s);
        });
    }

    @Override
    public List<CbChatMessage> getHistory(UUID sessionId) {
        return messageRepo
            .findBySessionIdOrderBySentAtAsc(sessionId);
    }

    // ─────────────────────────────────────────────
    // Route intent to correct DB query
    // ─────────────────────────────────────────────
    private String resolveDynamicAnswer(
            String intent,
            String externalUserId,
            String appId) {

        return switch (intent) {

            // USER intents
            case "track_order",
                 "order_status",
                 "delivery_delay",
                 "order_not_delivered" ->
                orderQueryService
                    .getActiveOrderStatus(externalUserId);

            case "order_history" ->
                orderQueryService
                    .getOrderHistory(externalUserId, appId);

            case "order_timeline" ->
                orderQueryService
                    .getOrderTimeline(externalUserId, appId);

            case "refund_status" ->
                paymentQueryService
                    .getRefundStatus(externalUserId, appId);

            case "money_deducted",
                 "payment_failed" ->
                paymentQueryService
                    .getPaymentFailureStatus(
                        externalUserId, appId);

            // VENDOR intents
            case "new_order",
                 "order_preparation_time",
                 "vendor_cancel_order",
                 "customer_cancelled" ->
                orderQueryService
                    .getVendorActiveOrder(externalUserId);

            case "vendor_order_history" ->
                orderQueryService
                    .getOrderHistory(externalUserId, appId);

            case "rider_not_arrived" ->
                orderQueryService
                    .getVendorActiveOrder(externalUserId);

            case "payout_status",
                 "payout_not_received" ->
                paymentQueryService
                    .getLatestPaymentStatus(
                        externalUserId, appId);

            // RIDER intents
            case "current_order",
                 "order_picked_status",
                 "delivery_completed" ->
                orderQueryService
                    .getRiderActiveOrder(externalUserId);

            case "earnings_today",
                 "payout_rider",
                 "payment_not_received_rider" ->
                paymentQueryService
                    .getLatestPaymentStatus(
                        externalUserId, appId);

            default -> "I am looking into this. "
                     + "Please hold on.";
        };
    }

    private String raiseTicket(
            CbChatSession session,
            String externalUserId,
            String appId,
            String message) {

        CbUser user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElse(null);

        CbTicket ticket = CbTicket.builder()
            .sessionId(session.getId())
            .cbUserId(user != null
                ? user.getId() : null)
            .appId(appId)
            .category("GENERAL")
            .description(message)
            .status("OPEN")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        ticketRepo.save(ticket);

        return "🎫 A support ticket has been created "
             + "for your query.\n"
             + "Our team will contact you within "
             + (appId.equals("RIDER") ? "1 hour"
                : appId.equals("VENDOR") ? "4 hours"
                : "2 hours")
             + ".\nIs there anything else I can help you with?";
    }

    private void saveMessage(
            UUID sessionId,
            String senderType,
            String message,
            String intent,
            UUID faqId) {

        messageRepo.save(CbChatMessage.builder()
            .sessionId(sessionId)
            .senderType(senderType)
            .message(message)
            .intent(intent)
            .matchedFaqId(faqId)
            .sentAt(LocalDateTime.now())
            .build());
    }

    private String getWelcomeMessage(String appId) {
        return switch (appId) {
            case "USER"   ->
                "👋 Hi! I am your support assistant.\n"
              + "I can help you with orders, payments, "
              + "delivery, and more.\nWhat can I help you with?";
            case "VENDOR" ->
                "👋 Hi! I am your vendor support assistant.\n"
              + "I can help with orders, payments, "
              + "store settings, and more.\nHow can I assist you?";
            case "RIDER"  ->
                "👋 Hi! I am your delivery support assistant.\n"
              + "I can help with deliveries, earnings, "
              + "and app issues.\nWhat do you need help with?";
            default ->
                "👋 Hi! How can I help you today?";
        };
    }

    private ChatResponse buildResponse(
            String reply,
            String intent,
            UUID sessionId,
            String status) {
        return ChatResponse.builder()
            .reply(reply)
            .intent(intent)
            .sessionId(sessionId)
            .status(status)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
