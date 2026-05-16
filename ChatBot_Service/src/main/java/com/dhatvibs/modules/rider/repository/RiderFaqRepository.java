package com.dhatvibs.modules.rider.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.dhatvibs.modules.rider.entities.RiderFaq;

import java.util.List;
import java.util.UUID;

@Repository
public interface RiderFaqRepository
        extends JpaRepository<RiderFaq, UUID> {

    @Query("""
        SELECT f FROM RiderFaq f
        WHERE f.isActive = TRUE
        AND f.isCategoryHeader = TRUE
        ORDER BY f.displayOrder ASC
        """)
    List<RiderFaq> findAllCategories();

    @Query("""
        SELECT f FROM RiderFaq f
        WHERE f.category = :category
        AND f.isActive = TRUE
        AND (f.isCategoryHeader = FALSE
             OR f.isCategoryHeader IS NULL)
        ORDER BY f.priority DESC
        """)
    List<RiderFaq> findQuestionsByCategory(
            @Param("category") String category);

    List<RiderFaq> findByIsActiveTrue();
}