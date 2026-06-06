package com.dhatvibs.modules.riderchatbot.repository;

import com.dhatvibs.modules.riderchatbot.entity
        .RiderChatbotTicket;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface RiderChatbotTicketRepository
        extends JpaRepository<RiderChatbotTicket,
                UUID> {

    Page<RiderChatbotTicket> findByStatus(
            String status, Pageable pageable);

    long countByStatus(String status);
}