package com.dhatvibs.modules.repository.chat;

/*
 * import java.util.*;
 * 
 * import org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.stereotype.Repository;
 * 
 * import com.dhatvibs.modules.entities.chat.CbChatMessage;
 * 
 * @Repository public interface CbChatMessageRepository extends
 * JpaRepository<CbChatMessage, UUID> {
 * 
 * List<CbChatMessage> findBySessionIdOrderBySentAtAsc( UUID sessionId); }
 */ 


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbChatMessage;

import java.util.List;
import java.util.UUID;

@Repository
public interface CbChatMessageRepository
        extends JpaRepository<CbChatMessage, UUID> {

    List<CbChatMessage> findBySessionIdOrderBySentAtAsc(
            UUID sessionId);
}
