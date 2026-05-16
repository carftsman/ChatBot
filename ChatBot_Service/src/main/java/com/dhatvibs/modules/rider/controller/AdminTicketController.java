package com.dhatvibs.modules.rider.controller;


import com.dhatvibs.modules.rider.dto.TicketUpdateRequest;
import com.dhatvibs.modules.rider.entities.RiderTicket;
import com.dhatvibs.modules.rider.repository.RiderTicketRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/rider/api/admin/tickets")
@RequiredArgsConstructor
@Tag(name = "Admin",
     description = "Admin ticket management — no auth")
public class AdminTicketController {

    private final RiderTicketRepository ticketRepo;

    @Operation(summary = "Get all rider tickets",
        description = "Filter by status. No auth needed.")
    @GetMapping
    public ResponseEntity<Page<RiderTicket>>
            getTickets(
            @RequestParam(required = false)
                String status,
            @RequestParam(defaultValue = "0")
                int page,
            @RequestParam(defaultValue = "20")
                int size) {

        Pageable pageable = PageRequest.of(
            page, size,
            Sort.by("createdAt").descending());

        Page<RiderTicket> tickets =
            status != null
                ? ticketRepo.findByStatus(
                    status, pageable)
                : ticketRepo.findAll(pageable);

        return ResponseEntity.ok(tickets);
    }

    @Operation(summary = "Get ticket by ID")
    @GetMapping("/{ticketId}")
    public ResponseEntity<RiderTicket> getTicket(
            @PathVariable UUID ticketId) {
        return ResponseEntity.ok(
            ticketRepo.findById(ticketId)
                .orElseThrow(() ->
                    new RuntimeException(
                        "Ticket not found")));
    }

    @Operation(summary = "Update ticket status",
        description = "Assign or resolve ticket")
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<RiderTicket> update(
            @PathVariable UUID ticketId,
            @RequestBody TicketUpdateRequest req) {

        RiderTicket ticket = ticketRepo
            .findById(ticketId)
            .orElseThrow(() ->
                new RuntimeException(
                    "Ticket not found"));

        ticket.setStatus(req.getStatus());
        if (req.getAssignedTo() != null)
            ticket.setAssignedTo(req.getAssignedTo());
        if ("RESOLVED".equals(req.getStatus()))
            ticket.setResolvedAt(
                LocalDateTime.now());
        ticket.setUpdatedAt(LocalDateTime.now());

        return ResponseEntity.ok(
            ticketRepo.save(ticket));
    }

    @Operation(summary = "Get ticket stats")
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>>
            stats() {
        return ResponseEntity.ok(Map.of(
            "total",      ticketRepo.count(),
            "open",       ticketRepo.countByStatus("OPEN"),
            "inProgress", ticketRepo.countByStatus("IN_PROGRESS"),
            "resolved",   ticketRepo.countByStatus("RESOLVED")));
    }
}
