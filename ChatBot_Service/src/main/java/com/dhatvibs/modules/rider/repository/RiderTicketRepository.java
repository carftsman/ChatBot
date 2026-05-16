package com.dhatvibs.modules.rider.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.rider.entities.RiderTicket;

import java.util.UUID;

@Repository
public interface RiderTicketRepository
        extends JpaRepository<RiderTicket, UUID> {

    Page<RiderTicket> findByStatus(
            String status, Pageable pageable);

    long countByStatus(String status);
}
