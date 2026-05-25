package com.dhatvibs.modules.vendor.respository;


import com.dhatvibs.modules.vendor.entity
        .VendorChatMessage;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface VendorChatMessageRepository
        extends JpaRepository<VendorChatMessage,
                UUID> {

    List<VendorChatMessage>
        findBySessionIdOrderBySentAtAsc(
                UUID sessionId);
}