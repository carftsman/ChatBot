package com.dhatvibs.modules.repository.chat;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.entities.chat.CbFaq;

@Repository
public interface CbFaqRepository
        extends JpaRepository<CbFaq, UUID> {
    List<CbFaq> findByAppIdInAndIsActiveTrue(List<String> appIds);
}