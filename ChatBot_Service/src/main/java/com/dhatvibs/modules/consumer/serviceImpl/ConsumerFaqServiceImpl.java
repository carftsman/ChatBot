package com.dhatvibs.modules.consumer.serviceImpl;


import com.dhatvibs.modules.consumer.dto.*;
import com.dhatvibs.modules.consumer.entity.*;
import com.dhatvibs.modules.consumer.repository.*;
import com.dhatvibs.modules.consumer.service.*;
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
public class ConsumerFaqServiceImpl
        implements ConsumerFaqService {

    private final ConsumerFaqRepository      faqRepo;
    private final ConsumerChatMessageRepository
                                             messageRepo;
    private final ConsumerTicketRepository   ticketRepo;
    private final ConsumerChatSessionRepository
                                             sessionRepo;
    private final ConsumerQueryService       queryService;

    @Override
    public List<ConsumerCategoryResponse>
            getCategories() {
        return faqRepo.findAllCategories()
            .stream()
            .map(f -> ConsumerCategoryResponse
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
    public List<ConsumerFaqQuestionResponse>
            getQuestions(String category) {
        return faqRepo.findQuestionsByCategory(
                category)
            .stream()
            .map(f -> ConsumerFaqQuestionResponse
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
    public ConsumerFaqAnswerResponse getAnswer(
            UUID faqId, UUID sessionId) {

        ConsumerFaq faq = faqRepo.findById(faqId)
            .orElseThrow(() ->
                new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Question not found"));

        String  answer      = null;
        boolean needsTicket = false;
        String  ticketId    = null;

        // Get token and orderId from session
        String consumerToken = null;
        String contextOrderId = null;
        String consumerId = null;

        if (sessionId != null) {
            Optional<ConsumerChatSession> session =
                sessionRepo.findById(sessionId);
            if (session.isPresent()) {
                consumerToken =
                    session.get().getConsumerToken();
                contextOrderId =
                    session.get().getContextOrderId();
                consumerId =
                    session.get().getConsumerId();
            }
        }

        if (isSupportIntent(faq.getIntent())) {
            // Raise ticket
            ConsumerTicket ticket = raiseTicket(
                sessionId, consumerId,
                contextOrderId,
                faq.getQuestion(),
                faq.getCategory());

            answer = "Support ticket #"
                + ticket.getId().toString()
                    .substring(0, 8).toUpperCase()
                + " created.\n"
                + "Our team will contact you "
                + "within 2 hours.";
            needsTicket = true;
            ticketId = ticket.getId().toString();

        } else if (Boolean.TRUE.equals(
                faq.getNeedsApi())) {
            answer = resolveFromApi(
                faq.getIntent(),
                consumerToken,
                contextOrderId,
                consumerId);
        } else {
            answer = faq.getAnswer();
        }

        // Save to history
        if (sessionId != null) {
            saveMessage(sessionId, "USER",
                faq.getQuestion(),
                faq.getIntent(), faqId, "FAQ");
            saveMessage(sessionId, "BOT",
                answer, faq.getIntent(),
                faqId, "FAQ");
        }

        return ConsumerFaqAnswerResponse.builder()
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
            String intent,
            String token,
            String orderId,
            String consumerId) {

        if (token == null)
            return "Session expired. "
                 + "Please login again.";

        log.info("Resolving intent: {}", intent);

        return switch (intent) {
            case "order_status"        ->
                queryService.getOrderDetails(
                    orderId, token);
		/*
		 * case "order_history" -> queryService.getRecentOrder( token, consumerId);
		 */
                
            case "order_history" ->    
                queryService.getOrderDetails(
                		orderId, token);
                
            case "cancel_order"        ->
                queryService.cancelOrder(
                    orderId, token);
            case "order_not_delivered" ->
                queryService.getOrderNotDelivered(
                    orderId, token);
            case "refund_status"       ->
                queryService.getRefundStatus(
                    orderId, token);
            case "payment_failed"      ->
                queryService.getPaymentStatus(
                    orderId, token);
            case "consumer_profile"    ->
                queryService.getConsumerProfile(
                    token, consumerId);
            case "my_addresses"        ->
                queryService.getAddresses(token);
            default ->
                "I am checking this for you.";
        };
    }

    private boolean isSupportIntent(String intent) {
        return switch (intent) {
            case "talk_to_agent",
                 "emergency",
                 "fallback",
                 "login_issue" -> true;
            default -> false;
        };
    }

    private ConsumerTicket raiseTicket(
            UUID sessionId,
            String consumerId,
            String orderId,
            String description,
            String category) {

        ConsumerTicket ticket =
            ConsumerTicket.builder()
                .sessionId(sessionId)
                .consumerId(consumerId)
                .orderId(orderId)
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
            ConsumerChatMessage.builder()
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
