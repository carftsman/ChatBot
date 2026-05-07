package com.dhatvibs.modules.repository.chat;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbChatSession;

@Repository
public interface CbChatSessionRepository
        extends JpaRepository<CbChatSession, UUID> {

    Optional<CbChatSession> findByIdAndStatus(
            UUID id, String status);
}
