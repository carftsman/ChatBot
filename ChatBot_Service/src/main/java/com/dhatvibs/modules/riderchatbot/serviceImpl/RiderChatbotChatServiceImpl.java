package com.dhatvibs.modules.riderchatbot.serviceImpl;

import com.dhatvibs.modules.riderchatbot.dto.*;
import com.dhatvibs.modules.riderchatbot.entity.*;
import com.dhatvibs.modules.riderchatbot.repository.*;
import com.dhatvibs.modules.riderchatbot.service.*;
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
public class RiderChatbotChatServiceImpl
        implements RiderChatbotChatService {

    private final RiderChatbotSessionRepository
                                sessionRepo;
    private final RiderChatbotMessageRepository
                                messageRepo;
    private final RiderChatbotTicketRepository
                                ticketRepo;
    private final RiderChatbotQueryService
                                queryService;
    private final SimpMessagingTemplate messaging;

    @Override
    public RiderChatbotChatStartResponse startSession(
            String riderId,
            String riderToken,
            String orderId) {

        log.info("startSession → riderId: {} "
               + "| orderId: {}",
                 riderId, orderId);

        // Reuse open session for same orderId
        if (orderId != null && !orderId.isBlank()) {
            Optional<RiderChatbotSession> existing =
                sessionRepo.findOpenSessionByOrderId(
                    orderId);
            if (existing.isPresent()) {
                RiderChatbotSession s = existing.get();
                s.setRiderToken(riderToken);
                sessionRepo.save(s);
                return RiderChatbotChatStartResponse
                    .builder()
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

        RiderChatbotSession session =
            RiderChatbotSession.builder()
                .riderId(riderId)
                .riderToken(riderToken)
                .contextOrderId(orderId)
                .status("OPEN")
                .chatEnabled(false)
                .startedAt(LocalDateTime.now())
                .build();

        session = sessionRepo.save(session);

        String welcome = (orderId != null
                && !orderId.isBlank())
            ? "Hi! I can see order "
              + orderId + ".\n"
              + "Please select a category below."
            : "Hi! I am your rider support.\n"
              + "Please select a category below.";

        saveMessage(session.getId(), "SYSTEM",
            welcome, null, null, "SYSTEM");

        return RiderChatbotChatStartResponse.builder()
            .sessionId(session.getId())
            .status("OPEN")
            .welcomeMessage(welcome)
            .chatEnabled(false)
            .contextOrderId(orderId)
            .startedAt(session.getStartedAt())
            .build();
    }

    @Override
    public RiderChatbotSessionStatusResponse
            resolveOrEscalate(
            UUID sessionId,
            boolean resolved,
            String riderId) {

        RiderChatbotSession session =
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

            return RiderChatbotSessionStatusResponse
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

            // Build chat summary
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

            RiderChatbotTicket ticket =
                RiderChatbotTicket.builder()
                    .sessionId(sessionId)
                    .riderId(riderId)
                    .orderId(
                        session.getContextOrderId())
                    .chatSummary(sb.toString())
                    .category("SUPPORT")
                    .description(
                        "Rider clicked Issue "
                        + "Not Resolved.")
                    .status("OPEN")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            RiderChatbotTicket saved =
                ticketRepo.save(ticket);

            String msg =
                "Your issue has been escalated.\n"
                + "Our support team will contact "
                + "you within 1 hour.\n"
                + "Ticket ID: TKT-"
                + saved.getId().toString()
                    .substring(0, 8).toUpperCase();

            saveMessage(sessionId, "SYSTEM",
                msg, null, null, "SYSTEM");
            broadcast(sessionId, msg,
                "SYSTEM", "ESCALATED");

            return RiderChatbotSessionStatusResponse
                .builder()
                .sessionId(sessionId)
                .status("OPEN")
                .chatEnabled(true)
                .resolutionType("ESCALATED")
                .message(msg)
                .timestamp(LocalDateTime.now())
                .build();
        }
    }

    @Override
    public RiderChatbotWebSocketResponse
            processMessage(
            RiderChatbotWebSocketRequest request) {

        RiderChatbotSession session =
            sessionRepo.findById(
                    request.getSessionId())
                .orElseThrow(() ->
                    new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Session not found"));

        if (!Boolean.TRUE.equals(
                session.getChatEnabled())) {
            RiderChatbotWebSocketResponse blocked =
                RiderChatbotWebSocketResponse.builder()
                    .reply("Please use FAQ options.")
                    .sessionId(session.getId())
                    .senderType("SYSTEM")
                    .status("OPEN")
                    .timestamp(LocalDateTime.now())
                    .build();
            messaging.convertAndSend(
                "/topic/riderchatbot/"
                + session.getId(), blocked);
            return blocked;
        }

        saveMessage(session.getId(), "USER",
            request.getMessage(),
            null, null, "FREE_CHAT");

        String reply = resolveMessage(
            request.getMessage(),
            session.getRiderToken(),
            session.getContextOrderId());

        saveMessage(session.getId(), "BOT",
            reply, null, null, "FREE_CHAT");

        RiderChatbotWebSocketResponse response =
            RiderChatbotWebSocketResponse.builder()
                .reply(reply)
                .sessionId(session.getId())
                .senderType("BOT")
                .status("OPEN")
                .timestamp(LocalDateTime.now())
                .build();

        messaging.convertAndSend(
            "/topic/riderchatbot/"
            + session.getId(), response);

        return response;
    }

    @Override
    public RiderChatbotOrderHistoryResponse
            getHistoryByOrderId(
            String orderId, int page, int size) {

        List<RiderChatbotSession> sessions =
            sessionRepo.findAllByOrderId(orderId);

        if (sessions.isEmpty())
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "No chat history for order: "
                + orderId);

        String latestStatus =
            sessions.get(0).getStatus();

        List<RiderChatbotOrderHistoryResponse
                .MessageDto> allMessages =
            sessions.stream()
                .flatMap(s ->
                    messageRepo
                        .findBySessionIdOrderBySentAtAsc(
                            s.getId())
                        .stream()
                        .map(m ->
                            RiderChatbotOrderHistoryResponse
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
                    RiderChatbotOrderHistoryResponse
                        .MessageDto::getSentAt))
                .collect(Collectors.toList());

        long total      = allMessages.size();
        int  totalPages = (int) Math.ceil(
            (double) total / size);
        int  from       = page * size;
        int  to         = Math.min(
            from + size, (int) total);

        List<RiderChatbotOrderHistoryResponse
                .MessageDto> paged =
            from >= total
                ? List.of()
                : allMessages.subList(from, to);

        return RiderChatbotOrderHistoryResponse
            .builder()
            .orderId(orderId)
            .latestStatus(latestStatus)
            .page(page)
            .size(size)
            .totalMessages(total)
            .totalPages(totalPages)
            .hasNext(page + 1 < totalPages)
            .hasPrevious(page > 0)
            .messages(paged)
            .build();
    }

    private String resolveMessage(
            String message, String token,
            String orderId) {
        String lower = message.toLowerCase().trim();

        if (lower.contains("earning"))
            return queryService
                .getEarningsSummary(token);
        if (lower.contains("today"))
            return queryService
                .getDailyEarnings(token);
        if (lower.contains("week"))
            return queryService
                .getWeeklyEarnings(token);
        if (lower.contains("cod")
                || lower.contains("cash"))
            return queryService.getCashBalance(token);
        if (lower.contains("order")
                && lower.contains("histor"))
            return queryService.getOrderHistory(token);
        if (lower.contains("stat"))
            return queryService.getOrderStats(token);
        if (lower.contains("order"))
            return queryService.getOrderDetails(
                orderId, token);
        if (lower.contains("rating"))
            return queryService.getRatings(token);
        if (lower.contains("slot"))
            return queryService.getActiveSlots(token);
        if (lower.contains("profile"))
            return queryService.getRiderProfile(token);
        if (lower.contains("wallet"))
            return queryService.getWalletBalance(token);
        if (lower.contains("bank"))
            return queryService.getBankDetails(token);
        if (lower.contains("incentive")
                || lower.contains("bonus"))
            return queryService.getIncentives(token);
        if (lower.contains("referral")
                || lower.contains("refer"))
            return queryService
                .getReferralSummary(token);

        return "I am here to help. "
             + "Please describe your issue.";
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

    private void broadcast(UUID sessionId,
            String reply, String senderType,
            String status) {
        messaging.convertAndSend(
            "/topic/riderchatbot/" + sessionId,
            RiderChatbotWebSocketResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .senderType(senderType)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build());
    }
}