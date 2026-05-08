package com.dhatvibs.modules.serviceImpl.chat;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.dhatvibs.modules.dto.chat.*;
import com.dhatvibs.modules.entities.auth.CbUser;
import com.dhatvibs.modules.entities.chat.CbChatMessage;
import com.dhatvibs.modules.entities.chat.CbFaq;
import com.dhatvibs.modules.entities.chat.CbTicket;
import com.dhatvibs.modules.repository.auth.CbUserRepository;
import com.dhatvibs.modules.repository.chat.*;
import com.dhatvibs.modules.service.chat.FaqService;
import com.dhatvibs.modules.service.chat.OrderQueryService;
import com.dhatvibs.modules.service.chat.PaymentQueryService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FaqServiceImpl implements FaqService {

    private final CbFaqRepository         faqRepo;
    private final CbChatMessageRepository messageRepo;
    private final CbTicketRepository      ticketRepo;
    private final CbUserRepository        userRepo;
    private final CbChatSessionRepository sessionRepo;
    private final OrderQueryService      orderQueryService;
    private final PaymentQueryService     paymentQueryService;

    // ─────────────────────────────────────────────
    // STEP 1 — Get categories for this role
    // Called when chat opens after user taps order
    // ─────────────────────────────────────────────
    @Override
    public List<CategoryResponse> getCategories(
            String appId) {

        List<CbFaq> categories =
            faqRepo.findCategoriesByAppId(appId);

        if (categories.isEmpty()) {
            log.warn("No categories found for appId: {}",
                     appId);
        }

        return categories.stream()
            .map(f -> CategoryResponse.builder()
                .category(f.getCategory())
                .displayName(f.getQuestion())
                .displayOrder(
                    f.getDisplayOrder() != null
                        ? f.getDisplayOrder() : 0)
                .build())
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // STEP 2 — Get questions under a category
    // Called when user taps a category button
    // ─────────────────────────────────────────────
    @Override
    public List<FaqQuestionResponse> getQuestions(
            String appId, String category) {

        List<CbFaq> questions =
            faqRepo.findQuestionsByAppIdAndCategory(
                appId, category);

        if (questions.isEmpty()) {
            log.warn("No questions found for "
                   + "appId: {} category: {}",
                     appId, category);
        }

        return questions.stream()
            .map(f -> FaqQuestionResponse.builder()
                .faqId(f.getId())
                .question(f.getQuestion())
                .category(f.getCategory())
                .intent(f.getIntent())
                .displayOrder(
                    f.getDisplayOrder() != null
                        ? f.getDisplayOrder() : 0)
                .build())
            .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // STEP 3 — Get answer when user taps a question
    // Fetches from DB if needs_db = TRUE
    // Raises ticket for support intents
    // Saves messages to session history
    // ─────────────────────────────────────────────
    @Override
    public FaqAnswerResponse getAnswer(
            UUID faqId,
            String externalUserId,
            String appId,
            UUID contextOrderId,
            UUID sessionId) {

        // Find the FAQ by id
        CbFaq faq = faqRepo.findById(faqId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Question not found"));

        String  answer      = null;
        boolean needsTicket = false;
        String  ticketId    = null;

        // ── Determine answer type ─────────────────
        if (isSupportIntent(faq.getIntent())) {
            // Auto raise ticket for support intents
            CbTicket ticket = raiseTicket(
                sessionId,
                externalUserId,
                appId,
                contextOrderId,
                faq.getQuestion());

            String shortId = ticket.getId()
                .toString()
                .substring(0, 8)
                .toUpperCase();

            answer = "🎫 Support ticket #" + shortId
                   + " has been created.\n"
                   + "Our team will contact you within "
                   + getSlaTime(appId) + ".\n"
                   + "Is there anything else I can "
                   + "help you with?";

            needsTicket = true;
            ticketId    = ticket.getId().toString();

        } else if (Boolean.TRUE.equals(faq.getNeedsDb())) {
            // Fetch live data from DB tables
            answer = resolveDynamicAnswer(
                faq.getIntent(),
                externalUserId,
                appId,
                contextOrderId);

        } else {
            // Static answer from cb_faqs.answer column
            answer = faq.getAnswer();
        }

        // ── Save to session history ───────────────
        if (sessionId != null) {
            // Save user question
            saveMessage(
                sessionId,
                "USER",
                faq.getQuestion(),
                faq.getIntent(),
                faq.getId(),
                "FAQ");

            // Save bot answer
            saveMessage(
                sessionId,
                "BOT",
                answer,
                faq.getIntent(),
                faq.getId(),
                "FAQ");
        }

        log.info("FAQ answered → intent: {} | "
               + "needsDb: {} | appId: {} | userId: {}",
                faq.getIntent(),
                faq.getNeedsDb(),
                appId,
                externalUserId);

        return FaqAnswerResponse.builder()
            .faqId(faq.getId())
            .question(faq.getQuestion())
            .answer(answer)
            .intent(faq.getIntent())
            .category(faq.getCategory())
            .needsTicket(needsTicket)
            .ticketId(ticketId)
            .build();
    }

    // ─────────────────────────────────────────────
    // Route intent → correct DB query service
    // Uses contextOrderId for order-specific answers
    // ─────────────────────────────────────────────
    private String resolveDynamicAnswer(
            String intent,
            String externalUserId,
            String appId,
            UUID contextOrderId) {

        return switch (intent) {

            // ── ORDER intents ─────────────────────
            case "track_order",
                 "order_status",
                 "delivery_delay",
                 "order_not_delivered" ->
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

            // ── PAYMENT intents ───────────────────
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

            // ── VENDOR intents ────────────────────
            case "new_order",
                 "order_preparation_time",
                 "customer_cancelled",
                 "rider_not_arrived" ->
                orderQueryService.getOrderStatus(
                    externalUserId, appId,
                    contextOrderId);

            case "payout_status",
                 "payout_not_received" ->
                paymentQueryService
                    .getLatestPaymentStatus(
                        externalUserId, appId,
                        contextOrderId);

            // ── RIDER intents ─────────────────────
            case "current_order",
                 "order_picked_status",
                 "delivery_completed" ->
                orderQueryService.getOrderStatus(
                    externalUserId, appId,
                    contextOrderId);

            case "earnings_today",
                 "payout_rider",
                 "payment_not_received_rider" ->
                paymentQueryService
                    .getLatestPaymentStatus(
                        externalUserId, appId,
                        contextOrderId);

            default ->
                "I am checking this for you. "
              + "Please hold on.";
        };
    }

    // ─────────────────────────────────────────────
    // Check if intent needs ticket
    // ─────────────────────────────────────────────
    private boolean isSupportIntent(String intent) {
        return switch (intent) {
            case "talk_to_agent",
                 "vendor_talk_to_agent",
                 "rider_talk_to_agent",
                 "fallback",
                 "missing_item",
                 "wrong_item",
                 "damaged_item",
                 "false_complaint",
                 "customer_misbehavior",
                 "emergency" -> true;
            default -> false;
        };
    }

    // ─────────────────────────────────────────────
    // Raise support ticket
    // ─────────────────────────────────────────────
    private CbTicket raiseTicket(
            UUID sessionId,
            String externalUserId,
            String appId,
            UUID contextOrderId,
            String description) {

        CbUser user = userRepo
            .findByExternalUserIdAndAppId(
                externalUserId, appId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"));

        CbTicket ticket = CbTicket.builder()
            .sessionId(sessionId)
            .cbUserId(user.getId())
            .cbOrderId(contextOrderId)
            .appId(appId)
            .category("GENERAL")
            .description(description)
            .status("OPEN")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        CbTicket saved = ticketRepo.save(ticket);

        log.info("Ticket raised → {} | appId: {} | "
               + "userId: {}",
                saved.getId(), appId, externalUserId);

        return saved;
    }

    // ─────────────────────────────────────────────
    // Save message to cb_chat_messages
    // ─────────────────────────────────────────────
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

    private String getSlaTime(String appId) {
        return switch (appId) {
            case "RIDER"  -> "1 hour";
            case "VENDOR" -> "4 hours";
            default       -> "2 hours";
        };
    }
}