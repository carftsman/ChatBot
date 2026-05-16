package com.dhatvibs.modules.rider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.rider.entities.RiderChatMessage;

import java.util.List;
import java.util.UUID;

@Repository
public interface RiderChatMessageRepository
        extends JpaRepository<RiderChatMessage, UUID> {

    List<RiderChatMessage>
        findBySessionIdOrderBySentAtAsc(
            UUID sessionId);
}
