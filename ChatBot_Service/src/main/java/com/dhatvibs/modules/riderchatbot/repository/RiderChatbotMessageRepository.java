package com.dhatvibs.modules.riderchatbot.repository;

import com.dhatvibs.modules.riderchatbot.entity
        .RiderChatbotMessage;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface RiderChatbotMessageRepository
        extends JpaRepository<RiderChatbotMessage,
                UUID> {

    List<RiderChatbotMessage>
        findBySessionIdOrderBySentAtAsc(
                UUID sessionId);
}