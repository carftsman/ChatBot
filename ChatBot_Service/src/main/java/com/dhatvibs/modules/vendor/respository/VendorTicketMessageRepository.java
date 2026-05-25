package com.dhatvibs.modules.vendor.respository;


import com.dhatvibs.modules.vendor.entity
        .VendorTicketMessage;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface VendorTicketMessageRepository
        extends JpaRepository<VendorTicketMessage,
                UUID> {

    List<VendorTicketMessage>
        findByTicketIdOrderBySentAtAsc(UUID ticketId);
}
