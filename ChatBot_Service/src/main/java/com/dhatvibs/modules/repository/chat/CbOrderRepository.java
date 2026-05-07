package com.dhatvibs.modules.repository.chat;

/*
 * import java.util.*;
 * 
 * import org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.data.jpa.repository.Query; import
 * org.springframework.stereotype.Repository;
 * 
 * import com.dhatvibs.modules.entities.chat.CbOrder;
 * 
 * import feign.Param;
 * 
 * @Repository public interface CbOrderRepository extends JpaRepository<CbOrder,
 * UUID> {
 * 
 * // Latest active order for user
 * 
 * @Query(""" SELECT o FROM CbOrder o WHERE o.cbUserId = :userId AND
 * o.orderStatus NOT IN ( 'DELIVERED','CANCELLED_BY_USER',
 * 'CANCELLED_BY_VENDOR','CANCELLED_BY_SYSTEM') ORDER BY o.placedAt DESC """)
 * Optional<CbOrder> findActiveOrderByUserId(
 * 
 * @Param("userId") UUID userId);
 * 
 * // Last 5 orders for history List<CbOrder>
 * findTop5ByCbUserIdOrderByPlacedAtDesc( UUID cbUserId);
 * 
 * // Vendor active orders
 * 
 * @Query(""" SELECT o FROM CbOrder o WHERE o.cbVendorId = :vendorId AND
 * o.orderStatus NOT IN ( 'DELIVERED','CANCELLED_BY_USER',
 * 'CANCELLED_BY_VENDOR','CANCELLED_BY_SYSTEM') ORDER BY o.placedAt DESC """)
 * Optional<CbOrder> findActiveOrderByVendorId(
 * 
 * @Param("vendorId") UUID vendorId);
 * 
 * // Rider active order
 * 
 * @Query(""" SELECT o FROM CbOrder o WHERE o.cbRiderId = :riderId AND
 * o.orderStatus IN ( 'RIDER_ASSIGNED','PICKED_UP','OUT_FOR_DELIVERY') ORDER BY
 * o.placedAt DESC """) Optional<CbOrder> findActiveOrderByRiderId(
 * 
 * @Param("riderId") UUID riderId);
 * 
 * // By external order id Optional<CbOrder> findByExternalOrderId( String
 * externalOrderId); }
 */ 


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbOrder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CbOrderRepository
        extends JpaRepository<CbOrder, UUID> {

    // USER — recent orders
    List<CbOrder> findTop5ByCbUserIdOrderByPlacedAtDesc(
            UUID cbUserId);

    // VENDOR — recent orders
    List<CbOrder> findTop5ByCbVendorIdOrderByPlacedAtDesc(
            UUID cbVendorId);

    // RIDER — recent orders
    List<CbOrder> findTop5ByCbRiderIdOrderByPlacedAtDesc(
            UUID cbRiderId);

    // Active order by userId
    @Query("""
        SELECT o FROM CbOrder o
        WHERE o.cbUserId = :userId
        AND o.orderStatus NOT IN (
            'DELIVERED','CANCELLED_BY_USER',
            'CANCELLED_BY_VENDOR','CANCELLED_BY_SYSTEM')
        ORDER BY o.placedAt DESC
        """)
    Optional<CbOrder> findActiveOrderByUserId(
            @Param("userId") UUID userId);

    // Active order by vendorId
    @Query("""
        SELECT o FROM CbOrder o
        WHERE o.cbVendorId = :vendorId
        AND o.orderStatus NOT IN (
            'DELIVERED','CANCELLED_BY_USER',
            'CANCELLED_BY_VENDOR','CANCELLED_BY_SYSTEM')
        ORDER BY o.placedAt DESC
        """)
    Optional<CbOrder> findActiveOrderByVendorId(
            @Param("vendorId") UUID vendorId);

    // Active order by riderId
    @Query("""
        SELECT o FROM CbOrder o
        WHERE o.cbRiderId = :riderId
        AND o.orderStatus IN (
            'RIDER_ASSIGNED','PICKED_UP',
            'OUT_FOR_DELIVERY')
        ORDER BY o.placedAt DESC
        """)
    Optional<CbOrder> findActiveOrderByRiderId(
            @Param("riderId") UUID riderId);

    Optional<CbOrder> findByExternalOrderId(
            String externalOrderId);
}
