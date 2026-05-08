package com.dhatvibs.modules.repository.chat;

/*
 * import java.util.*;
 * 
 * import org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.stereotype.Repository;
 * 
 * import com.dhatvibs.modules.entities.chat.CbChatSession;
 * 
 * @Repository public interface CbChatSessionRepository extends
 * JpaRepository<CbChatSession, UUID> {
 * 
 * Optional<CbChatSession> findByIdAndStatus( UUID id, String status); }
 */ 

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbChatSession;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CbChatSessionRepository
        extends JpaRepository<CbChatSession, UUID> {

    Optional<CbChatSession> findByIdAndStatus(
            UUID id, String status);

    // All sessions for a user — for history
    @Query("""
        SELECT s FROM CbChatSession s
        WHERE s.cbUserId = :userId
        ORDER BY s.startedAt DESC
        """)
    List<CbChatSession> findAllByUserId(
            @Param("userId") UUID userId);

    // All open sessions for a user
    Optional<CbChatSession> findByCbUserIdAndStatus(
            UUID cbUserId, String status);
}