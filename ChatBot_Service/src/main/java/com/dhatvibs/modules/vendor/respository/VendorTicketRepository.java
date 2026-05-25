package com.dhatvibs.modules.vendor.respository;


import com.dhatvibs.modules.vendor.entity
        .VendorTicket;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface VendorTicketRepository
        extends JpaRepository<VendorTicket, UUID> {

    Page<VendorTicket> findByStatus(
            String status, Pageable pageable);

    long countByStatus(String status);
}
