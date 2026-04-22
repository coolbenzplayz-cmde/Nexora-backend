package org.example.nexora.media.repository;

import org.example.nexora.media.entity.ContentTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContentTemplateRepository extends JpaRepository<ContentTemplate, Long> {
    
    List<ContentTemplate> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    List<ContentTemplate> findByIsPublicOrderByUsageCountDesc(Boolean isPublic);
    
    @Query("SELECT t FROM ContentTemplate t WHERE t.isPublic = true OR t.userId = :userId ORDER BY t.usageCount DESC")
    List<ContentTemplate> findByIsPublicOrUserId(@Param("userId") Long userId);
    
    @Query("SELECT t FROM ContentTemplate t WHERE (t.isPublic = true OR t.userId = :userId) AND t.templateType = :templateType ORDER BY t.usageCount DESC")
    List<ContentTemplate> findByTemplateTypeAndIsPublicOrUserId(@Param("templateType") ContentTemplate.TemplateType templateType, @Param("userId") Long userId);
    
    List<ContentTemplate> findByCategoryAndIsPublicOrderByRatingDesc(String category, Boolean isPublic);
    
    @Query("SELECT t FROM ContentTemplate t WHERE t.isPublic = true AND (LOWER(t.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.tags) LIKE LOWER(CONCAT('%', :search, '%')))")
    List<ContentTemplate> searchPublicTemplates(@Param("search") String search);
}
