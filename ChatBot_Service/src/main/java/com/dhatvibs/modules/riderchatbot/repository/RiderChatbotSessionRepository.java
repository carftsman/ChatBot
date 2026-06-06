package com.dhatvibs.modules.riderchatbot.repository;

import com.dhatvibs.modules.riderchatbot.entity
        .RiderChatbotSession;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface RiderChatbotSessionRepository
        extends JpaRepository<RiderChatbotSession,
                UUID> {

    @Query("""
        SELECT s FROM RiderChatbotSession s
        WHERE s.contextOrderId = :orderId
        ORDER BY s.startedAt DESC
        """)
    List<RiderChatbotSession> findAllByOrderId(
            @Param("orderId") String orderId);

    @Query("""
        SELECT s FROM RiderChatbotSession s
        WHERE s.contextOrderId = :orderId
        AND s.status = 'OPEN'
        ORDER BY s.startedAt DESC
        """)
    Optional<RiderChatbotSession>
        findOpenSessionByOrderId(
            @Param("orderId") String orderId);

    @Query("""
        SELECT s FROM RiderChatbotSession s
        WHERE s.riderId = :riderId
        ORDER BY s.startedAt DESC
        """)
    List<RiderChatbotSession> findAllByRiderId(
            @Param("riderId") String riderId);
}