/*
 * package com.dhatvibs.modules.consumer.repository;
 * 
 * 
 * import com.dhatvibs.modules.consumer.entity .ConsumerChatSession; import
 * org.springframework.data.jpa.repository .JpaRepository; import
 * org.springframework.data.jpa.repository.Query; import
 * org.springframework.data.repository.query.Param; import
 * org.springframework.stereotype.Repository; import java.util.*;
 * 
 * @Repository public interface ConsumerChatSessionRepository extends
 * JpaRepository<ConsumerChatSession, UUID> {
 * 
 * Optional<ConsumerChatSession> findByContextOrderId(String contextOrderId);
 * 
 * @Query(""" SELECT s FROM ConsumerChatSession s WHERE s.consumerId =
 * :consumerId ORDER BY s.startedAt DESC """) List<ConsumerChatSession>
 * findAllByConsumerId(
 * 
 * @Param("consumerId") String consumerId); }
 */


package com.dhatvibs.modules.consumer.repository;

import com.dhatvibs.modules.consumer.entity
        .ConsumerChatSession;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface ConsumerChatSessionRepository
        extends JpaRepository<ConsumerChatSession,
                UUID> {

    // Get ALL sessions for an order
    // — returns most recent first
    @Query("""
        SELECT s FROM ConsumerChatSession s
        WHERE s.contextOrderId = :orderId
        ORDER BY s.startedAt DESC
        """)
    List<ConsumerChatSession> findAllByOrderId(
            @Param("orderId") String orderId);

    // Get latest OPEN session for an order
    @Query("""
        SELECT s FROM ConsumerChatSession s
        WHERE s.contextOrderId = :orderId
        AND s.status = 'OPEN'
        ORDER BY s.startedAt DESC
        """)
    Optional<ConsumerChatSession>
        findOpenSessionByOrderId(
            @Param("orderId") String orderId);

    // Get all sessions for a consumer
    @Query("""
        SELECT s FROM ConsumerChatSession s
        WHERE s.consumerId = :consumerId
        ORDER BY s.startedAt DESC
        """)
    List<ConsumerChatSession> findAllByConsumerId(
            @Param("consumerId") String consumerId);
}