package com.dhatvibs.modules.repository.chat;

/*
 * import java.util.List; import java.util.UUID;
 * 
 * import org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.stereotype.Repository;
 * 
 * import com.dhatvibs.modules.entities.chat.CbFaq;
 * 
 * @Repository public interface CbFaqRepository extends JpaRepository<CbFaq,
 * UUID> { List<CbFaq> findByAppIdInAndIsActiveTrue(List<String> appIds); }
 */ 


/*
 * import org.springframework.data.jpa.repository.JpaRepository; import
 * org.springframework.data.jpa.repository.Query; import
 * org.springframework.data.repository.query.Param; import
 * org.springframework.stereotype.Repository;
 * 
 * import com.dhatvibs.modules.entities.chat.CbFaq;
 * 
 * import java.util.List; import java.util.UUID;
 * 
 * @Repository public interface CbFaqRepository extends JpaRepository<CbFaq,
 * UUID> {
 * 
 * // Used by IntentDetectorService // Fetch all active FAQs for appId
 * List<CbFaq> findByAppIdInAndIsActiveTrue( List<String> appIds);
 * 
 * // Used by FaqServiceImpl.getCategories() // Fetch only category headers for
 * this role
 * 
 * @Query(""" SELECT f FROM CbFaq f WHERE f.appId = :appId AND f.isActive = TRUE
 * AND f.isCategoryHeader = TRUE ORDER BY f.displayOrder ASC """) List<CbFaq>
 * findCategoriesByAppId(
 * 
 * @Param("appId") String appId);
 * 
 * // Used by FaqServiceImpl.getQuestions() // Fetch questions under a category
 * for this role // Excludes category headers
 * 
 * @Query(""" SELECT f FROM CbFaq f WHERE f.appId = :appId AND f.category =
 * :category AND f.isActive = TRUE AND (f.isCategoryHeader = FALSE OR
 * f.isCategoryHeader IS NULL) ORDER BY f.priority DESC """) List<CbFaq>
 * findQuestionsByAppIdAndCategory(
 * 
 * @Param("appId") String appId,
 * 
 * @Param("category") String category); }
 */  



import com.dhatvibs.modules.entities.chat.CbFaq;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CbFaqRepository
        extends JpaRepository<CbFaq, UUID> {

    // Used by IntentDetectorService
    List<CbFaq> findByAppIdInAndIsActiveTrue(
            List<String> appIds);

    // Used by FaqServiceImpl.getCategories()
    @Query("""
        SELECT f FROM CbFaq f
        WHERE f.appId = :appId
        AND f.isActive = TRUE
        AND f.isCategoryHeader = TRUE
        ORDER BY f.displayOrder ASC
        """)
    List<CbFaq> findCategoriesByAppId(
            @Param("appId") String appId);

    // Used by FaqServiceImpl.getQuestions()
    @Query("""
        SELECT f FROM CbFaq f
        WHERE f.appId = :appId
        AND f.category = :category
        AND f.isActive = TRUE
        AND (f.isCategoryHeader = FALSE
             OR f.isCategoryHeader IS NULL)
        ORDER BY f.priority DESC
        """)
    List<CbFaq> findQuestionsByAppIdAndCategory(
            @Param("appId") String appId,
            @Param("category") String category);
}