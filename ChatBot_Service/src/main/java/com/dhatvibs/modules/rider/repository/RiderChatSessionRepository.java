package com.dhatvibs.modules.rider.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.rider.entities.RiderChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RiderChatSessionRepository
        extends JpaRepository<RiderChatSession, UUID> {

    Optional<RiderChatSession> findByIdAndStatus(
            UUID id, String status);

    @Query("""
        SELECT s FROM RiderChatSession s
        WHERE s.riderId = :riderId
        ORDER BY s.startedAt DESC
        """)
    List<RiderChatSession> findAllByRiderId(
            @Param("riderId") String riderId);
    
    
 // Add to RiderChatSessionRepository.java
    Optional<RiderChatSession> findByContextOrderId(
            String contextOrderId);
}