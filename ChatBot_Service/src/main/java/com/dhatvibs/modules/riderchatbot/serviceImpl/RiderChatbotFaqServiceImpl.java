package com.dhatvibs.modules.riderchatbot.serviceImpl;

import com.dhatvibs.modules.riderchatbot.dto.*;
import com.dhatvibs.modules.riderchatbot.entity.*;
import com.dhatvibs.modules.riderchatbot.repository.*;
import com.dhatvibs.modules.riderchatbot.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server
        .ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderChatbotFaqServiceImpl
        implements RiderChatbotFaqService {

    private final RiderChatbotFaqRepository     faqRepo;
    private final RiderChatbotMessageRepository messageRepo;
    private final RiderChatbotTicketRepository  ticketRepo;
    private final RiderChatbotSessionRepository sessionRepo;
    private final RiderChatbotQueryService      queryService;

    @Override
    public List<RiderChatbotCategoryResponse>
            getCategories() {
        return faqRepo.findAllCategories()
            .stream()
            .map(f -> RiderChatbotCategoryResponse
                .builder()
                .category(f.getCategory())
                .displayName(f.getQuestion())
                .displayOrder(
                    f.getDisplayOrder() != null
                        ? f.getDisplayOrder() : 0)
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public List<RiderChatbotFaqQuestionResponse>
            getQuestions(String category) {
        return faqRepo.findQuestionsByCategory(
                category)
            .stream()
            .map(f -> RiderChatbotFaqQuestionResponse
                .builder()
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

    @Override
    public RiderChatbotFaqAnswerResponse getAnswer(
            UUID faqId, UUID sessionId) {

        RiderChatbotFaq faq = faqRepo.findById(faqId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Question not found"));

        String  answer      = null;
        boolean needsTicket = false;
        String  ticketId    = null;

        String riderToken     = null;
        String contextOrderId = null;
        String riderId        = null;

        if (sessionId != null) {
            Optional<RiderChatbotSession> session =
                sessionRepo.findById(sessionId);
            if (session.isPresent()) {
                riderToken =
                    session.get().getRiderToken();
                contextOrderId =
                    session.get().getContextOrderId();
                riderId =
                    session.get().getRiderId();
            }
        }

        if (isSupportIntent(faq.getIntent())) {
            RiderChatbotTicket ticket = raiseTicket(
                sessionId, riderId,
                contextOrderId,
                faq.getQuestion(),
                faq.getCategory(),
                riderToken);

            answer = "Your issue has been escalated.\n"
                + "Our support team will contact "
                + "you within 1 hour.\n"
                + "Ticket ID: TKT-"
                + ticket.getId().toString()
                    .substring(0, 8).toUpperCase();
            needsTicket = true;
            ticketId = ticket.getId().toString();

        } else if (Boolean.TRUE.equals(
                faq.getNeedsApi())) {
            answer = resolveFromApi(
                faq.getIntent(),
                riderToken,
                contextOrderId);
        } else {
            answer = faq.getAnswer();
        }

        if (sessionId != null) {
            saveMessage(sessionId, "USER",
                faq.getQuestion(),
                faq.getIntent(), faqId, "FAQ");
            saveMessage(sessionId, "BOT",
                answer, faq.getIntent(),
                faqId, "FAQ");
        }

        return RiderChatbotFaqAnswerResponse.builder()
            .faqId(faq.getId())
            .question(faq.getQuestion())
            .answer(answer)
            .intent(faq.getIntent())
            .category(faq.getCategory())
            .needsTicket(needsTicket)
            .ticketId(ticketId)
            .build();
    }

    private String resolveFromApi(
            String intent, String token,
            String orderId) {

        if (token == null)
            return "Session expired. Login again.";

        log.info("resolveFromApi intent: {}", intent);

        return switch (intent) {
            case "earnings_summary" ->
                queryService.getEarningsSummary(token);
            case "earnings_daily"   ->
                queryService.getDailyEarnings(token);
            case "earnings_weekly"  ->
                queryService.getWeeklyEarnings(token);
            case "cod_balance"      ->
                queryService.getCashBalance(token);
            case "wallet_balance"   ->
                queryService.getWalletBalance(token);
            case "order_history"    ->
                queryService.getOrderHistory(token);
            case "order_stats"      ->
                queryService.getOrderStats(token);
            case "order_details"    ->
                queryService.getOrderDetails(
                    orderId, token);
            case "ratings"          ->
                queryService.getRatings(token);
            case "weekly_performance" ->
                queryService.getWeeklyPerformance(
                    token);
            case "active_slots"     ->
                queryService.getActiveSlots(token);
            case "slot_history"     ->
                queryService.getSlotHistory(token);
            case "rider_profile"    ->
                queryService.getRiderProfile(token);
            case "bank_details"     ->
                queryService.getBankDetails(token);
            case "documents"        ->
                queryService.getDocuments(token);
            case "incentives"       ->
                queryService.getIncentives(token);
            case "referral_summary" ->
                queryService.getReferralSummary(token);
            default ->
                "I am checking this for you.";
        };
    }

    private boolean isSupportIntent(String intent) {
        return switch (intent) {
            case "talk_to_agent",
                 "fallback",
                 "login_issue" -> true;
            default -> false;
        };
    }

    private RiderChatbotTicket raiseTicket(
            UUID sessionId, String riderId,
            String orderId, String description,
            String category, String token) {

        String riderName  = "N/A";
        String riderPhone = "N/A";

        if (token != null) {
            try {
                com.fasterxml.jackson.databind
                    .JsonNode payload =
                    new com.fasterxml.jackson
                        .databind.ObjectMapper()
                        .readTree(new String(
                            java.util.Base64
                                .getDecoder()
                                .decode(
                                    token.split("\\.")[1]
                                    + "==")));
                riderPhone = payload
                    .path("phoneNumber").asText("N/A");
            } catch (Exception ignored) {}
        }

        RiderChatbotTicket ticket =
            RiderChatbotTicket.builder()
                .sessionId(sessionId)
                .riderId(riderId)
                .riderName(riderName)
                .riderPhone(riderPhone)
                .orderId(orderId)
                .chatSummary(
                    buildChatSummary(sessionId))
                .category(category)
                .description(description)
                .status("OPEN")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return ticketRepo.save(ticket);
    }

    private String buildChatSummary(UUID sessionId) {
        if (sessionId == null) return "";
        StringBuilder sb = new StringBuilder();
        messageRepo
            .findBySessionIdOrderBySentAtAsc(
                sessionId)
            .forEach(m -> {
                if (!"SYSTEM".equals(
                        m.getSenderType())) {
                    sb.append(m.getSenderType())
                      .append(": ")
                      .append(m.getMessage())
                      .append("\n");
                }
            });
        return sb.toString();
    }

    private void saveMessage(
            UUID sessionId, String senderType,
            String message, String intent,
            UUID faqId, String messageType) {
        messageRepo.save(
            RiderChatbotMessage.builder()
                .sessionId(sessionId)
                .senderType(senderType)
                .message(message)
                .intent(intent)
                .matchedFaqId(faqId)
                .messageType(messageType)
                .sentAt(LocalDateTime.now())
                .build());
    }
}