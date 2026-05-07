package com.dhatvibs.modules.repository.chat;

import java.util.*;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbOrderStatusHistory;

@Repository
public interface CbOrderStatusHistoryRepository
        extends JpaRepository<CbOrderStatusHistory, UUID> {

    List<CbOrderStatusHistory> findByCbOrderIdOrderByChangedAtAsc(
            UUID cbOrderId);
}
