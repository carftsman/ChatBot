package com.dhatvibs.modules.consumer.repository;


import com.dhatvibs.modules.consumer.entity.ConsumerFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ConsumerFaqRepository
        extends JpaRepository<ConsumerFaq, UUID> {

    @Query("""
        SELECT f FROM ConsumerFaq f
        WHERE f.isActive = TRUE
        AND f.isCategoryHeader = TRUE
        ORDER BY f.displayOrder ASC
        """)
    List<ConsumerFaq> findAllCategories();

    @Query("""
        SELECT f FROM ConsumerFaq f
        WHERE f.category = :category
        AND f.isActive = TRUE
        AND (f.isCategoryHeader = FALSE
             OR f.isCategoryHeader IS NULL)
        ORDER BY f.priority DESC
        """)
    List<ConsumerFaq> findQuestionsByCategory(
            @Param("category") String category);
}