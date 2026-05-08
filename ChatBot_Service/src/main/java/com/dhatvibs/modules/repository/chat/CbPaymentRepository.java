package com.dhatvibs.modules.repository.chat;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbPayment;

import org.springframework.data.repository.query.Param;

@Repository
public interface CbPaymentRepository
        extends JpaRepository<CbPayment, UUID> {

    Optional<CbPayment> findByCbOrderId(UUID cbOrderId);

    List<CbPayment> findByCbUserIdOrderByCreatedAtDesc(
            UUID cbUserId);

    @Query("""
        SELECT p FROM CbPayment p
        WHERE p.cbUserId = :userId
        AND p.paymentStatus IN ('REFUND_INITIATED',
            'REFUND_PROCESSING','REFUNDED')
        ORDER BY p.createdAt DESC
        """)
    List<CbPayment> findRefundsByUserId(
            @Param("userId") UUID userId);

    @Query("""
        SELECT p FROM CbPayment p
        WHERE p.cbUserId = :userId
        AND p.paymentStatus IN ('FAILED','CANCELLED')
        ORDER BY p.createdAt DESC
        """)
    List<CbPayment> findFailedPaymentsByUserId(
            @Param("userId") UUID userId);
}