package com.dhatvibs.modules.riderchatbot.controller;

import com.dhatvibs.modules.riderchatbot.entity
        .RiderChatbotTicket;
import com.dhatvibs.modules.riderchatbot.repository
        .RiderChatbotTicketRepository;
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
@RequestMapping("/riderchatbot/admin/tickets")
@RequiredArgsConstructor
@Tag(name = "Rider Chatbot Admin",
     description = "Admin ticket management "
         + "— no auth needed")
public class RiderChatbotAdminController {

    private final RiderChatbotTicketRepository
        ticketRepo;

    @Operation(summary = "Get all OPEN tickets")
    @GetMapping
    public ResponseEntity<
            Page<RiderChatbotTicket>> getTickets(
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

    @Operation(
        summary = "Get full ticket details",
        description = """
            Returns ticket with:
            - Rider ID, name, phone
            - Order ID
            - Full chat summary
            Admin uses this before calling rider.
            """)
    @GetMapping("/{ticketId}")
    public ResponseEntity<RiderChatbotTicket>
            getTicket(
            @PathVariable UUID ticketId) {
        return ResponseEntity.ok(
            ticketRepo.findById(ticketId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Ticket not found")));
    }

    @Operation(
        summary = "Resolve ticket",
        description = """
            Ticket status → RESOLVED.
            Disappears from admin panel.
            """)
    @PutMapping("/{ticketId}/resolve")
    public ResponseEntity<Map<String, Object>>
            resolveTicket(
            @PathVariable UUID ticketId,
            @RequestBody(required = false)
                Map<String, String> body) {

        RiderChatbotTicket ticket = ticketRepo
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

        return ResponseEntity.ok(Map.of(
            "message",    "Ticket resolved",
            "ticketId",   ticketId.toString(),
            "status",     "RESOLVED",
            "resolvedAt", LocalDateTime.now()
                              .toString()));
    }

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