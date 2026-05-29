/*
 * package com.dhatvibs.modules.vendor.controller;
 * 
 * import com.dhatvibs.modules.vendor.dto.*; import
 * com.dhatvibs.modules.vendor.entity.*; import
 * com.dhatvibs.modules.vendor.respository.*;
 * 
 * import io.swagger.v3.oas.annotations.Operation; import
 * io.swagger.v3.oas.annotations.tags.Tag; import
 * lombok.RequiredArgsConstructor; import lombok.extern.slf4j.Slf4j; import
 * org.springframework.data.domain.*; import
 * org.springframework.http.ResponseEntity; import
 * org.springframework.messaging.simp .SimpMessagingTemplate; import
 * org.springframework.web.bind.annotation.*;
 * 
 * import java.time.LocalDateTime; import java.util.*;
 * 
 * @Slf4j
 * 
 * @RestController
 * 
 * @RequestMapping("/vendor/admin/tickets")
 * 
 * @RequiredArgsConstructor
 * 
 * @Tag(name = "Vendor Admin", description = "Vendor ticket management " +
 * "— no auth needed") public class VendorAdminController {
 * 
 * private final VendorTicketRepository ticketRepo; private final
 * VendorTicketMessageRepository ticketMessageRepo; private final
 * VendorChatMessageRepository chatMessageRepo; private final
 * SimpMessagingTemplate messaging;
 * 
 * @Operation(summary = "Get all vendor tickets")
 * 
 * @GetMapping public ResponseEntity<Page<VendorTicket>> getTickets(
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
 * @Operation( summary = "Get ticket with full chat history")
 * 
 * @GetMapping("/{ticketId}") public ResponseEntity<
 * VendorAdminTicketDetailResponse> getTicket(
 * 
 * @PathVariable UUID ticketId) {
 * 
 * VendorTicket ticket = ticketRepo .findById(ticketId) .orElseThrow(() -> new
 * RuntimeException( "Ticket not found"));
 * 
 * List<VendorTicketMessage> messages = ticketMessageRepo
 * .findByTicketIdOrderBySentAtAsc( ticketId);
 * 
 * StringBuilder chatHistory = new StringBuilder(); if (ticket.getSessionId() !=
 * null) { chatMessageRepo .findBySessionIdOrderBySentAtAsc(
 * ticket.getSessionId()) .forEach(m -> chatHistory .append(m.getSenderType())
 * .append(": ") .append(m.getMessage()) .append("\n")); }
 * 
 * return ResponseEntity.ok( VendorAdminTicketDetailResponse.builder()
 * .ticket(ticket) .messages(messages) .chatHistory(chatHistory.toString())
 * .build()); }
 * 
 * @Operation(summary = "Admin reply to vendor")
 * 
 * @PostMapping("/{ticketId}/reply") public
 * ResponseEntity<VendorAdminReplyResponse> adminReply(
 * 
 * @PathVariable UUID ticketId,
 * 
 * @RequestBody VendorAdminReplyRequest req) {
 * 
 * VendorTicketMessage msg = VendorTicketMessage.builder() .ticketId(ticketId)
 * .senderType("ADMIN") .senderId(req.getAdminId() != null ? req.getAdminId() :
 * "SUPPORT") .message(req.getMessage()) .sentAt(LocalDateTime.now()) .build();
 * 
 * VendorTicketMessage saved = ticketMessageRepo.save(msg);
 * 
 * messaging.convertAndSend( "/topic/vendor/ticket/" + ticketId,
 * VendorAdminReplyResponse.builder() .messageId(saved.getId())
 * .ticketId(ticketId) .message(req.getMessage()) .senderType("ADMIN")
 * .sentAt(saved.getSentAt()) .build());
 * 
 * return ResponseEntity.ok( VendorAdminReplyResponse.builder()
 * .messageId(saved.getId()) .ticketId(ticketId) .message(req.getMessage())
 * .senderType("ADMIN") .sentAt(saved.getSentAt()) .build()); }
 * 
 * @Operation(summary = "Vendor reply to ticket")
 * 
 * @PostMapping("/{ticketId}/vendor-reply") public
 * ResponseEntity<VendorAdminReplyResponse> vendorReply(
 * 
 * @PathVariable UUID ticketId,
 * 
 * @RequestBody VendorAdminReplyRequest req) {
 * 
 * VendorTicketMessage msg = VendorTicketMessage.builder() .ticketId(ticketId)
 * .senderType("VENDOR") .senderId(req.getAdminId()) .message(req.getMessage())
 * .sentAt(LocalDateTime.now()) .build();
 * 
 * VendorTicketMessage saved = ticketMessageRepo.save(msg);
 * 
 * messaging.convertAndSend( "/topic/admin/vendor/ticket/" + ticketId,
 * VendorAdminReplyResponse.builder() .messageId(saved.getId())
 * .ticketId(ticketId) .message(req.getMessage()) .senderType("VENDOR")
 * .sentAt(saved.getSentAt()) .build());
 * 
 * return ResponseEntity.ok( VendorAdminReplyResponse.builder()
 * .messageId(saved.getId()) .ticketId(ticketId) .message(req.getMessage())
 * .senderType("VENDOR") .sentAt(saved.getSentAt()) .build()); }
 * 
 * @Operation(summary = "Update ticket status")
 * 
 * @PutMapping("/{ticketId}/status") public ResponseEntity<VendorTicket> update(
 * 
 * @PathVariable UUID ticketId,
 * 
 * @RequestBody VendorTicketUpdateRequest req) {
 * 
 * VendorTicket ticket = ticketRepo .findById(ticketId) .orElseThrow(() -> new
 * RuntimeException( "Ticket not found"));
 * 
 * ticket.setStatus(req.getStatus()); if (req.getAssignedTo() != null)
 * ticket.setAssignedTo(req.getAssignedTo()); if
 * ("RESOLVED".equals(req.getStatus())) {
 * ticket.setResolvedAt(LocalDateTime.now()); messaging.convertAndSend(
 * "/topic/vendor/ticket/" + ticketId, VendorAdminReplyResponse.builder()
 * .ticketId(ticketId) .message("Your ticket has been " + "resolved.")
 * .senderType("ADMIN") .sentAt(LocalDateTime.now()) .build()); }
 * ticket.setUpdatedAt(LocalDateTime.now());
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

package com.dhatvibs.modules.vendor.controller;

import com.dhatvibs.modules.vendor.entity.VendorTicket;
import com.dhatvibs.modules.vendor.respository.VendorTicketRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/vendor/admin/tickets")
@RequiredArgsConstructor
@Tag(name = "Vendor Admin",
     description = "Vendor ticket management "
         + "— no auth needed")
public class VendorAdminController {

    private final VendorTicketRepository ticketRepo;

    // ── GET ALL OPEN TICKETS ──────────────────────
    @Operation(
        summary = "Get all OPEN tickets",
        description = """
            Returns only OPEN tickets by default.
            Admin sees these in the panel.
            Resolved tickets are hidden.
            """)
    @GetMapping
    public ResponseEntity<Page<VendorTicket>>
            getTickets(
            @RequestParam(defaultValue = "OPEN")
                String status,
            @RequestParam(defaultValue = "0")
                int page,
            @RequestParam(defaultValue = "50")
                int size) {

        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("createdAt").descending());

        return ResponseEntity.ok(
            ticketRepo.findByStatus(
                status, pageable));
    }

    // ── GET TICKET WITH FULL DETAILS ──────────────
    @Operation(
        summary = "Get full ticket details",
        description = """
            Returns complete ticket with:
            - Vendor name, phone, email
            - Order number, status, amount, store
            - Full chat summary
            Admin uses this before contacting vendor.
            """)
    @GetMapping("/{ticketId}")
    public ResponseEntity<VendorTicket> getTicket(
            @PathVariable UUID ticketId) {

        return ResponseEntity.ok(
            ticketRepo.findById(ticketId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Ticket not found")));
    }

    // ── RESOLVE TICKET — disappears from panel ────
    @Operation(
        summary = "Resolve ticket",
        description = """
            Admin calls this after solving the issue.
            Ticket status changes to RESOLVED.
            Ticket disappears from admin panel.
            """)
    @PutMapping("/{ticketId}/resolve")
    public ResponseEntity<Map<String, Object>>
            resolveTicket(
            @PathVariable UUID ticketId,
            @RequestBody(required = false)
                Map<String, String> body) {

        VendorTicket ticket = ticketRepo
            .findById(ticketId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Ticket not found"));

        ticket.setStatus("RESOLVED");
        ticket.setResolvedAt(LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        if (body != null
                && body.get("assignedTo") != null) {
            ticket.setAssignedTo(
                body.get("assignedTo"));
        }

        ticketRepo.save(ticket);

        log.info("Ticket resolved: {}", ticketId);

        return ResponseEntity.ok(Map.of(
            "message",
                "Ticket resolved successfully",
            "ticketId",
                ticketId.toString(),
            "status",
                "RESOLVED",
            "resolvedAt",
                LocalDateTime.now().toString()));
    }

    // ── TICKET STATS ──────────────────────────────
    @Operation(summary = "Get ticket stats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>>
            stats() {
        return ResponseEntity.ok(Map.of(
            "open",     ticketRepo.countByStatus("OPEN"),
            "resolved", ticketRepo.countByStatus("RESOLVED"),
            "total",    ticketRepo.count()));
    }
}