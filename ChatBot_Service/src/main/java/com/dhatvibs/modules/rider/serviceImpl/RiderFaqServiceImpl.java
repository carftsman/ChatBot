package com.dhatvibs.modules.rider.serviceImpl;


import com.dhatvibs.modules.rider.dto.*;
import com.dhatvibs.modules.rider.entities.*;
import com.dhatvibs.modules.rider.repository.*;
import com.dhatvibs.modules.rider.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RiderFaqServiceImpl
        implements RiderFaqService {

    private final RiderFaqRepository         faqRepo;
    private final RiderChatMessageRepository messageRepo;
    private final RiderTicketRepository      ticketRepo;
    private final RiderChatSessionRepository sessionRepo;
    private final RiderQueryService          queryService;

    @Override
    public List<CategoryResponse> getCategories() {
        return faqRepo.findAllCategories()
            .stream()
            .map(f -> CategoryResponse.builder()
                .category(f.getCategory())
                .displayName(f.getQuestion())
                .displayOrder(
                    f.getDisplayOrder() != null
                        ? f.getDisplayOrder() : 0)
                .build())
            .collect(Collectors.toList());
    }

    @Override
    public List<FaqQuestionResponse> getQuestions(
            String category) {
        return faqRepo.findQuestionsByCategory(
                category)
            .stream()
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

    @Override
    public FaqAnswerResponse getAnswer(
            UUID faqId, UUID sessionId) {

        RiderFaq faq = faqRepo.findById(faqId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Question not found"));

        String answer;
        boolean needsTicket = false;
        String  ticketId    = null;

        // Get rider token from session
        String riderToken = sessionId != null
            ? sessionRepo.findById(sessionId)
                .map(RiderChatSession::getRiderToken)
                .orElse(null)
            : null;

        if (isSupportIntent(faq.getIntent())) {
            // Raise ticket
            RiderTicket ticket = raiseTicket(
                sessionId, faq.getQuestion(),
                faq.getCategory(), riderToken);

            answer = "Support ticket #"
                + ticket.getId().toString()
                    .substring(0, 8).toUpperCase()
                + " created.\n"
                + "Our team will contact you "
                + "within 1 hour.";
            needsTicket = true;
            ticketId = ticket.getId().toString();

        } else if (Boolean.TRUE.equals(
                faq.getNeedsApi())) {
            // Call Node.js API
            answer = resolveFromApi(
                faq.getIntent(), riderToken);
        } else {
            // Static answer from DB
            answer = faq.getAnswer();
        }

        // Save to session history
        if (sessionId != null) {
            saveMessage(sessionId, "USER",
                faq.getQuestion(),
                faq.getIntent(), faqId, "FAQ");
            saveMessage(sessionId, "BOT",
                answer, faq.getIntent(),
                faqId, "FAQ");
        }

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

	/*
	 * private String resolveFromApi( String intent, String token) { if (token ==
	 * null) return "Session expired. " + "Please login again.";
	 * 
	 * return switch (intent) { case "earnings_today" ->
	 * queryService.getDailyEarnings(token); case "earnings_weekly" ->
	 * queryService.getWeeklyEarnings(token); case "earnings_summary",
	 * "payout_status" -> queryService.getEarningsSummary(token); case
	 * "cash_balance" -> queryService.getCashBalance(token); case "order_stats",
	 * "current_order" -> queryService.getOrderStats(token); case "order_history" ->
	 * queryService.getOrderHistory(token); case "ratings" ->
	 * queryService.getRatings(token); case "weekly_performance"->
	 * queryService.getWeeklyPerformance(token); case "rider_profile" ->
	 * queryService.getRiderProfile(token); case "wallet_balance" ->
	 * queryService.getWalletBalance(token); default ->
	 * "I am checking this for you."; }; }
	 */
    
    private String resolveFromApi(
            String intent, String token) {

        if (token == null)
            return "Session expired. Please login again.";

        log.info("Resolving intent: {}", intent);

        return switch (intent) {

            // EARNINGS
            case "earnings_today"     ->
                queryService.getDailyEarnings(token);
            case "earnings_weekly"    ->
                queryService.getWeeklyEarnings(token);
            case "earnings_summary"   ->
                queryService.getEarningsSummary(token);
            case "cash_balance"       ->
                queryService.getCashBalance(token);
            case "wallet_balance"     ->
                queryService.getWalletBalance(token);

            // ORDERS
            case "order_stats",
                 "current_order"      ->
                queryService.getOrderStats(token);
            case "order_history"      ->
                queryService.getOrderHistory(token);

            // RATINGS
            case "ratings"            ->
                queryService.getRatings(token);
            case "weekly_performance" ->
                queryService.getWeeklyPerformance(token);

            // ACCOUNT
            case "rider_profile"      ->
                queryService.getRiderProfile(token);

            default -> {
                log.warn("Unmatched intent: {}", intent);
                yield "I am checking this for you. "
                    + "Please hold on.";
            }
        };
    }
    
    
    private boolean isSupportIntent(String intent) {
        return switch (intent) {
            case "talk_to_agent",
                 "emergency",
                 "fallback",
                 "login_issue",
                 "app_issue" -> true;
            default -> false;
        };
    }

    private RiderTicket raiseTicket(
            UUID sessionId,
            String description,
            String category,
            String riderToken) {

        String riderId = sessionId != null
            ? sessionRepo.findById(sessionId)
                .map(RiderChatSession::getRiderId)
                .orElse("unknown")
            : "unknown";

        RiderTicket ticket = RiderTicket.builder()
            .sessionId(sessionId)
            .riderId(riderId)
            .category(category)
            .description(description)
            .status("OPEN")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        return ticketRepo.save(ticket);
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
}
