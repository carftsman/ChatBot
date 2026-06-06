package com.dhatvibs.modules.riderchatbot.repository;

import com.dhatvibs.modules.riderchatbot.entity
        .RiderChatbotFaq;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface RiderChatbotFaqRepository
        extends JpaRepository<RiderChatbotFaq, UUID> {

    @Query("""
        SELECT f FROM RiderChatbotFaq f
        WHERE f.isActive = TRUE
        AND f.isCategoryHeader = TRUE
        ORDER BY f.displayOrder ASC
        """)
    List<RiderChatbotFaq> findAllCategories();

    @Query("""
        SELECT f FROM RiderChatbotFaq f
        WHERE f.category = :category
        AND f.isActive = TRUE
        AND (f.isCategoryHeader = FALSE
             OR f.isCategoryHeader IS NULL)
        ORDER BY f.priority DESC
        """)
    List<RiderChatbotFaq> findQuestionsByCategory(
            @Param("category") String category);
}