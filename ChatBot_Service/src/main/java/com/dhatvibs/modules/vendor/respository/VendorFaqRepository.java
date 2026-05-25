package com.dhatvibs.modules.vendor.respository;


import com.dhatvibs.modules.vendor.entity.VendorFaq;
import org.springframework.data.jpa.repository
        .JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;

@Repository
public interface VendorFaqRepository
        extends JpaRepository<VendorFaq, UUID> {

    @Query("""
        SELECT f FROM VendorFaq f
        WHERE f.isActive = TRUE
        AND f.isCategoryHeader = TRUE
        ORDER BY f.displayOrder ASC
        """)
    List<VendorFaq> findAllCategories();

    @Query("""
        SELECT f FROM VendorFaq f
        WHERE f.category = :category
        AND f.isActive = TRUE
        AND (f.isCategoryHeader = FALSE
             OR f.isCategoryHeader IS NULL)
        ORDER BY f.priority DESC
        """)
    List<VendorFaq> findQuestionsByCategory(
            @Param("category") String category);
}