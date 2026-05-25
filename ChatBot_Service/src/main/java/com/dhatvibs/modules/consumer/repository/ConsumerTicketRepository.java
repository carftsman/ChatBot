package com.dhatvibs.modules.consumer.repository;


import com.dhatvibs.modules.consumer.entity
        .ConsumerTicket;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface ConsumerTicketRepository
        extends JpaRepository<ConsumerTicket, UUID> {

    Page<ConsumerTicket> findByStatus(
            String status, Pageable pageable);

    long countByStatus(String status);
}
