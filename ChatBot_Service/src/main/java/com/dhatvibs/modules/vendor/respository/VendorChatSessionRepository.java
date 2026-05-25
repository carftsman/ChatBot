package com.dhatvibs.modules.vendor.respository;


import com.dhatvibs.modules.vendor.entity
        .VendorChatSession;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface VendorChatSessionRepository
        extends JpaRepository<VendorChatSession,
                UUID> {

    @Query("""
        SELECT s FROM VendorChatSession s
        WHERE s.contextOrderId = :orderId
        ORDER BY s.startedAt DESC
        """)
    List<VendorChatSession> findAllByOrderId(
            @Param("orderId") String orderId);

    @Query("""
        SELECT s FROM VendorChatSession s
        WHERE s.contextOrderId = :orderId
        AND s.status = 'OPEN'
        ORDER BY s.startedAt DESC
        """)
    Optional<VendorChatSession>
        findOpenSessionByOrderId(
            @Param("orderId") String orderId);

    @Query("""
        SELECT s FROM VendorChatSession s
        WHERE s.vendorId = :vendorId
        ORDER BY s.startedAt DESC
        """)
    List<VendorChatSession> findAllByVendorId(
            @Param("vendorId") String vendorId);
}
