package com.dhatvibs.modules.repository.chat;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbTicket;

@Repository
public interface CbTicketRepository
        extends JpaRepository<CbTicket, UUID> {

    List<CbTicket> findByCbUserIdOrderByCreatedAtDesc(
            UUID cbUserId);
}
