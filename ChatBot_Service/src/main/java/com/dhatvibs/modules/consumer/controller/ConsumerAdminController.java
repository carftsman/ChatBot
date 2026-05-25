/*
 * package com.dhatvibs.modules.consumer.controller;
 * 
 * 
 * import com.dhatvibs.modules.consumer.dto.ConsumerTicketUpdateRequest; import
 * com.dhatvibs.modules.consumer.entity .ConsumerTicket; import
 * com.dhatvibs.modules.consumer.repository .ConsumerTicketRepository;
 * 
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import
 * lombok.RequiredArgsConstructor; import org.springframework.data.domain.*;
 * import org.springframework.http.ResponseEntity; import
 * org.springframework.web.bind.annotation.*; import java.time.LocalDateTime;
 * import java.util.*;
 * 
 * @RestController
 * 
 * @RequestMapping("/consumer/admin/tickets")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Consumer Admin", description = "Consumer ticket management " +
 * "— no auth needed") public class ConsumerAdminController {
 * 
 * private final ConsumerTicketRepository ticketRepo;
 * 
 * @Operation(summary = "Get all consumer tickets")
 * 
 * @GetMapping public ResponseEntity<Page<ConsumerTicket>> getTickets(
 * 
 * @RequestParam(required = false) String status,
 * 
 * @RequestParam(defaultValue = "0") int page,
 * 
 * @RequestParam(defaultValue = "50") int size) {
 * 
 * Pageable pageable = PageRequest.of( page, size,
 * Sort.by("createdAt").descending());
 * 
 * return ResponseEntity.ok( status != null ? ticketRepo.findByStatus( status,
 * pageable) : ticketRepo.findAll(pageable)); }
 * 
 * @Operation(summary = "Get ticket by ID")
 * 
 * @GetMapping("/{ticketId}") public ResponseEntity<ConsumerTicket> getTicket(
 * 
 * @PathVariable UUID ticketId) { return ResponseEntity.ok(
 * ticketRepo.findById(ticketId) .orElseThrow(() -> new RuntimeException(
 * "Ticket not found"))); }
 * 
 * @Operation(summary = "Update ticket status")
 * 
 * @PutMapping("/{ticketId}/status") public ResponseEntity<ConsumerTicket>
 * update(
 * 
 * @PathVariable UUID ticketId,
 * 
 * @RequestBody ConsumerTicketUpdateRequest req) {
 * 
 * ConsumerTicket ticket = ticketRepo .findById(ticketId) .orElseThrow(() -> new
 * RuntimeException( "Ticket not found"));
 * 
 * ticket.setStatus(req.getStatus()); if (req.getAssignedTo() != null)
 * ticket.setAssignedTo(req.getAssignedTo()); if
 * ("RESOLVED".equals(req.getStatus())) ticket.setResolvedAt(
 * LocalDateTime.now()); ticket.setUpdatedAt(LocalDateTime.now());
 * 
 * return ResponseEntity.ok( ticketRepo.save(ticket)); }
 * 
 * @Operation(summary = "Get ticket stats")
 * 
 * @GetMapping("/stats") public ResponseEntity<Map<String, Object>> stats() {
 * return ResponseEntity.ok(Map.of( "total", ticketRepo.count(), "open",
 * ticketRepo.countByStatus("OPEN"), "inProgress", ticketRepo.countByStatus(
 * "IN_PROGRESS"), "resolved", ticketRepo.countByStatus( "RESOLVED"))); } }
 */

package com.dhatvibs.modules.consumer.controller;

import com.dhatvibs.modules.consumer.dto.*;
import com.dhatvibs.modules.consumer.entity.*;
import com.dhatvibs.modules.consumer.repository.*;
import com.dhatvibs.modules.rider.dto.TicketUpdateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp
        .SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/consumer/admin/tickets")
@RequiredArgsConstructor
@Tag(name = "Consumer Admin",
     description = "Admin ticket management "
         + "— no auth needed")
public class ConsumerAdminController {

    private final ConsumerTicketRepository
                                ticketRepo;
    private final ConsumerTicketMessageRepository
                                ticketMessageRepo;
    private final ConsumerChatSessionRepository
                                sessionRepo;
    private final ConsumerChatMessageRepository
                                chatMessageRepo;
    private final SimpMessagingTemplate messaging;

    // ── GET ALL TICKETS ───────────────────────────
    @Operation(summary = "Get all consumer tickets")
    @GetMapping
    public ResponseEntity<Page<ConsumerTicket>>
            getTickets(
            @RequestParam(required = false)
                String status,
            @RequestParam(defaultValue = "0")
                int page,
            @RequestParam(defaultValue = "50")
                int size) {

        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("createdAt").descending());

        return ResponseEntity.ok(
            status != null
                ? ticketRepo.findByStatus(
                    status, pageable)
                : ticketRepo.findAll(pageable));
    }

    // ── GET TICKET WITH FULL CHAT HISTORY ─────────
    @Operation(
        summary = "Get ticket with full chat history",
        description = """
            Returns ticket details + all ticket messages
            + original chat history that led to this ticket.
            Admin sees the complete conversation context.
            """)
    @GetMapping("/{ticketId}")
    public ResponseEntity<AdminTicketDetailResponse>
            getTicket(
            @PathVariable UUID ticketId) {

        ConsumerTicket ticket = ticketRepo
            .findById(ticketId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Ticket not found"));

        // Get ticket messages (admin-consumer chat)
        List<ConsumerTicketMessage> ticketMessages =
            ticketMessageRepo
                .findByTicketIdOrderBySentAtAsc(
                    ticketId);

        // Get original chat history
        StringBuilder chatHistory =
            new StringBuilder();
        if (ticket.getSessionId() != null) {
            chatMessageRepo
                .findBySessionIdOrderBySentAtAsc(
                    ticket.getSessionId())
                .forEach(m -> chatHistory
                    .append(m.getSenderType())
                    .append(": ")
                    .append(m.getMessage())
                    .append("\n"));
        }

        return ResponseEntity.ok(
            AdminTicketDetailResponse.builder()
                .ticket(ticket)
                .messages(ticketMessages)
                .chatHistory(chatHistory.toString())
                .build());
    }

    // ── ADMIN REPLY — real-time WhatsApp style ─────
    @Operation(
        summary = "Admin reply to consumer",
        description = """
            Admin sends a message to consumer.
            Consumer receives it in real-time via WebSocket.
            Consumer subscribes to:
              /topic/consumer/ticket/{ticketId}
            """)
    @PostMapping("/{ticketId}/reply")
    public ResponseEntity<AdminReplyResponse>
            adminReply(
            @PathVariable UUID ticketId,
            @RequestBody AdminReplyRequest request) {

        ConsumerTicket ticket = ticketRepo
            .findById(ticketId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Ticket not found"));

        // Save admin message
        ConsumerTicketMessage msg =
            ConsumerTicketMessage.builder()
                .ticketId(ticketId)
                .senderType("ADMIN")
                .senderId(
                    request.getAdminId() != null
                        ? request.getAdminId()
                        : "SUPPORT")
                .message(request.getMessage())
                .sentAt(LocalDateTime.now())
                .build();

        ConsumerTicketMessage saved =
            ticketMessageRepo.save(msg);

        // Broadcast to consumer in real-time
        // Consumer subscribes to:
        // /topic/consumer/ticket/{ticketId}
        messaging.convertAndSend(
            "/topic/consumer/ticket/" + ticketId,
            AdminReplyResponse.builder()
                .messageId(saved.getId())
                .ticketId(ticketId)
                .message(request.getMessage())
                .senderType("ADMIN")
                .sentAt(saved.getSentAt())
                .build());

        log.info("Admin replied to ticket: {}",
                 ticketId);

        return ResponseEntity.ok(
            AdminReplyResponse.builder()
                .messageId(saved.getId())
                .ticketId(ticketId)
                .message(request.getMessage())
                .senderType("ADMIN")
                .sentAt(saved.getSentAt())
                .build());
    }

    // ── CONSUMER REPLY TO TICKET ──────────────────
    @Operation(
        summary = "Consumer reply to ticket",
        description = """
            Consumer sends message on a raised ticket.
            Admin receives it in real-time via WebSocket.
            Admin subscribes to:
              /topic/admin/ticket/{ticketId}
            """)
    @PostMapping("/{ticketId}/consumer-reply")
    public ResponseEntity<AdminReplyResponse>
            consumerReply(
            @PathVariable UUID ticketId,
            @RequestBody AdminReplyRequest request) {

        // Save consumer message on ticket
        ConsumerTicketMessage msg =
            ConsumerTicketMessage.builder()
                .ticketId(ticketId)
                .senderType("CONSUMER")
                .senderId(request.getAdminId())
                .message(request.getMessage())
                .sentAt(LocalDateTime.now())
                .build();

        ConsumerTicketMessage saved =
            ticketMessageRepo.save(msg);

        // Broadcast to admin in real-time
        messaging.convertAndSend(
            "/topic/admin/ticket/" + ticketId,
            AdminReplyResponse.builder()
                .messageId(saved.getId())
                .ticketId(ticketId)
                .message(request.getMessage())
                .senderType("CONSUMER")
                .sentAt(saved.getSentAt())
                .build());

        return ResponseEntity.ok(
            AdminReplyResponse.builder()
                .messageId(saved.getId())
                .ticketId(ticketId)
                .message(request.getMessage())
                .senderType("CONSUMER")
                .sentAt(saved.getSentAt())
                .build());
    }

    // ── UPDATE TICKET STATUS ──────────────────────
    @Operation(summary = "Update ticket status")
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<ConsumerTicket> update(
            @PathVariable UUID ticketId,
            @RequestBody TicketUpdateRequest req) {

        ConsumerTicket ticket = ticketRepo
            .findById(ticketId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Ticket not found"));

        ticket.setStatus(req.getStatus());
        if (req.getAssignedTo() != null)
            ticket.setAssignedTo(req.getAssignedTo());
        if ("RESOLVED".equals(req.getStatus()))
            ticket.setResolvedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        // Notify consumer ticket is resolved
        if ("RESOLVED".equals(req.getStatus())) {
            messaging.convertAndSend(
                "/topic/consumer/ticket/" + ticketId,
                AdminReplyResponse.builder()
                    .ticketId(ticketId)
                    .message("Your ticket has been "
                           + "resolved by our team.")
                    .senderType("ADMIN")
                    .sentAt(LocalDateTime.now())
                    .build());
        }

        return ResponseEntity.ok(
            ticketRepo.save(ticket));
    }

    // ── TICKET STATS ──────────────────────────────
    @Operation(summary = "Get ticket stats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>>
            stats() {
        return ResponseEntity.ok(Map.of(
            "total",      ticketRepo.count(),
            "open",       ticketRepo.countByStatus("OPEN"),
            "inProgress", ticketRepo.countByStatus(
                              "IN_PROGRESS"),
            "resolved",   ticketRepo.countByStatus(
                              "RESOLVED")));
    }
}