


package com.dhatvibs.modules.consumer.serviceImpl;

import com.dhatvibs.modules.consumer.dto.*;
import com.dhatvibs.modules.consumer.entity.*;
import com.dhatvibs.modules.consumer.repository.*;
import com.dhatvibs.modules.consumer.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp
        .SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server
        .ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConsumerChatServiceImpl
        implements ConsumerChatService {

    private final ConsumerChatSessionRepository
                                sessionRepo;
    private final ConsumerChatMessageRepository
                                messageRepo;
    private final ConsumerTicketRepository
                                ticketRepo;
    private final ConsumerQueryService
                                queryService;
    private final SimpMessagingTemplate messaging;

    @Override
    public ConsumerChatStartResponse startSession(
            String consumerId,
            String consumerToken,
            String orderId) {

        log.info("startSession → consumerId: {} "
               + "| orderId: {}",
                 consumerId, orderId);

        // Check if open session exists for this order
        // Reuse it instead of creating new one
        if (orderId != null && !orderId.isBlank()) {
            Optional<ConsumerChatSession> existing =
                sessionRepo.findOpenSessionByOrderId(
                    orderId);
            if (existing.isPresent()) {
                ConsumerChatSession s = existing.get();
                // Update token in case it changed
                s.setConsumerToken(consumerToken);
                sessionRepo.save(s);
                log.info("Reusing existing session: {}",
                         s.getId());

                return ConsumerChatStartResponse
                    .builder()
                    .sessionId(s.getId())
                    .status(s.getStatus())
                    .welcomeMessage(
                        "Welcome back! Continuing "
                        + "your support for order "
                        + orderId)
                    .chatEnabled(s.getChatEnabled())
                    .contextOrderId(orderId)
                    .startedAt(s.getStartedAt())
                    .build();
            }
        }

        // Create new session
        ConsumerChatSession session =
            ConsumerChatSession.builder()
                .consumerId(consumerId)
                .consumerToken(consumerToken)
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
            : "Hi! I am your support assistant.\n"
              + "Please select a category below.";

        saveMessage(session.getId(), "SYSTEM",
            welcome, null, null, "SYSTEM");

        return ConsumerChatStartResponse.builder()
            .sessionId(session.getId())
            .status("OPEN")
            .welcomeMessage(welcome)
            .chatEnabled(false)
            .contextOrderId(orderId)
            .startedAt(session.getStartedAt())
            .build();
    }

    @Override
    public ConsumerSessionStatusResponse
            resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String consumerId) {

        ConsumerChatSession session =
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

            return ConsumerSessionStatusResponse
                .builder()
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

            // Raise ticket immediately
            ConsumerTicket ticket =
                ConsumerTicket.builder()
                    .sessionId(sessionId)
                    .consumerId(consumerId)
                    .orderId(
                        session.getContextOrderId())
                    .category("SUPPORT")
                    .description(
                        "Consumer clicked Issue "
                        + "Not Resolved.")
                    .status("OPEN")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            ConsumerTicket saved =
                ticketRepo.save(ticket);

            String ticketNum = "TKT-"
                + saved.getId().toString()
                    .substring(0, 8).toUpperCase();

            String msg = "Free chat enabled.\n"
                + "Ticket " + ticketNum
                + " created.\n"
                + "Please describe your issue.";

            saveMessage(sessionId, "SYSTEM",
                msg, null, null, "SYSTEM");
            broadcast(sessionId, msg,
                "SYSTEM", "ESCALATED");

            return ConsumerSessionStatusResponse
                .builder()
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
    public ConsumerWebSocketResponse processMessage(
            ConsumerWebSocketRequest request) {

        ConsumerChatSession session =
            sessionRepo.findById(
                    request.getSessionId())
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        if (!Boolean.TRUE.equals(
                session.getChatEnabled())) {
            ConsumerWebSocketResponse blocked =
                ConsumerWebSocketResponse.builder()
                    .reply("Please use FAQ options.")
                    .sessionId(session.getId())
                    .senderType("SYSTEM")
                    .status("OPEN")
                    .timestamp(LocalDateTime.now())
                    .build();
            messaging.convertAndSend(
                "/topic/consumer/"
                + session.getId(), blocked);
            return blocked;
        }

        saveMessage(session.getId(), "USER",
            request.getMessage(),
            null, null, "FREE_CHAT");

        String reply = resolveFreeChatMessage(
            request.getMessage(),
            session.getConsumerToken(),
            session.getContextOrderId(),
            session.getConsumerId());

        saveMessage(session.getId(), "BOT",
            reply, null, null, "FREE_CHAT");

        ConsumerWebSocketResponse response =
            ConsumerWebSocketResponse.builder()
                .reply(reply)
                .sessionId(session.getId())
                .senderType("BOT")
                .status("OPEN")
                .timestamp(LocalDateTime.now())
                .build();

        messaging.convertAndSend(
            "/topic/consumer/"
            + session.getId(), response);

        return response;
    }

    // ── orderId-based history ─────────────────────
    // Industry standard — Swiggy/Zomato style
    // All sessions for same order merged into
    // one continuous timeline
	/*
	 * @Override public ConsumerOrderHistoryResponse getHistoryByOrderId(String
	 * orderId) {
	 * 
	 * List<ConsumerChatSession> sessions = sessionRepo.findAllByOrderId(orderId);
	 * 
	 * if (sessions.isEmpty()) throw new ResponseStatusException(
	 * HttpStatus.NOT_FOUND, "No chat history for order: " + orderId);
	 * 
	 * String latestStatus = sessions.get(0).getStatus();
	 * 
	 * // Merge ALL messages from ALL sessions // into one timeline — sorted by time
	 * List<ConsumerOrderHistoryResponse.MessageDto> allMessages = sessions.stream()
	 * .flatMap(s -> messageRepo .findBySessionIdOrderBySentAtAsc( s.getId())
	 * .stream() .map(m -> ConsumerOrderHistoryResponse .MessageDto.builder()
	 * .id(m.getId()) .senderType( m.getSenderType()) .message(m.getMessage())
	 * .messageType( m.getMessageType()) .intent(m.getIntent())
	 * .sentAt(m.getSentAt()) .sessionId( s.getId().toString()) .build()))
	 * .sorted(Comparator.comparing( ConsumerOrderHistoryResponse
	 * .MessageDto::getSentAt)) .collect(Collectors.toList());
	 * 
	 * return ConsumerOrderHistoryResponse.builder() .orderId(orderId)
	 * .latestStatus(latestStatus) .messages(allMessages) .build(); }
	 */
    
    @Override
    public ConsumerOrderHistoryResponse getHistoryByOrderId(
            String orderId,
            int page,
            int size) {

        List<ConsumerChatSession> sessions =
            sessionRepo.findAllByOrderId(orderId);

        if (sessions.isEmpty())
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No chat history for order: " + orderId);

        String latestStatus =
            sessions.get(0).getStatus();

        // Collect ALL messages from ALL sessions
        // merged into one timeline
        List<ConsumerOrderHistoryResponse.MessageDto>
            allMessages = sessions.stream()
                .flatMap(s ->
                    messageRepo
                        .findBySessionIdOrderBySentAtAsc(
                            s.getId())
                        .stream()
                        .map(m ->
                            ConsumerOrderHistoryResponse
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
                    ConsumerOrderHistoryResponse
                        .MessageDto::getSentAt))
                .collect(Collectors.toList());

        long totalMessages = allMessages.size();
        int  totalPages    = (int) Math.ceil(
            (double) totalMessages / size);

        // Apply pagination
        int fromIndex = page * size;
        int toIndex   = Math.min(
            fromIndex + size, (int) totalMessages);

        List<ConsumerOrderHistoryResponse.MessageDto>
            pagedMessages =
                fromIndex >= totalMessages
                    ? List.of()
                    : allMessages.subList(
                        fromIndex, toIndex);

        return ConsumerOrderHistoryResponse.builder()
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

    // ── PRIVATE HELPERS ───────────────────────────

    private String resolveFreeChatMessage(
            String message, String token,
            String orderId, String consumerId) {

        String lower = message.toLowerCase().trim();

        if (lower.contains("refund"))
            return queryService.getRefundStatus(
                orderId, token);
        if (lower.contains("cancel"))
            return queryService.cancelOrder(
                orderId, token);
        if (lower.contains("order status")
                || lower.contains("where is"))
            return queryService.getOrderDetails(
                orderId, token);
        if (lower.contains("not received")
                || lower.contains("not delivered"))
            return queryService.getOrderNotDelivered(
                orderId, token);
        if (lower.contains("payment"))
            return queryService.getPaymentStatus(
                orderId, token);

        return "I am here to help. "
             + "Could you describe your issue?";
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

    private void broadcast(
            UUID sessionId, String reply,
            String senderType, String status) {
        messaging.convertAndSend(
            "/topic/consumer/" + sessionId,
            ConsumerWebSocketResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .senderType(senderType)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build());
    }
}