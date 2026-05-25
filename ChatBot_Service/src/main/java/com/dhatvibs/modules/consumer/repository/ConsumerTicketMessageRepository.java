package com.dhatvibs.modules.consumer.repository;


import com.dhatvibs.modules.consumer.entity
        .ConsumerTicketMessage;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ConsumerTicketMessageRepository
        extends JpaRepository<
                ConsumerTicketMessage, UUID> {

    List<ConsumerTicketMessage>
        findByTicketIdOrderBySentAtAsc(UUID ticketId);
}