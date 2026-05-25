package com.dhatvibs.modules.consumer.repository;


import com.dhatvibs.modules.consumer.entity
        .ConsumerChatMessage;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ConsumerChatMessageRepository
        extends JpaRepository<ConsumerChatMessage,
                UUID> {

    List<ConsumerChatMessage>
        findBySessionIdOrderBySentAtAsc(
                UUID sessionId);
}