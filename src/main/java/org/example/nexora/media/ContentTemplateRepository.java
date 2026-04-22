package org.example.nexora.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Content template repository
 */
@Repository
public interface ContentTemplateRepository extends JpaRepository<ContentTemplate, Long> {
    
    List<ContentTemplate> findByCreatedByOrderByCreatedAtDesc(Long createdBy);
    
    List<ContentTemplate> findByTemplateTypeOrderByCreatedAtDesc(String templateType);
    
    List<ContentTemplate> findByCreatedByAndTemplateTypeOrderByCreatedAtDesc(Long createdBy, String templateType);
    
    boolean existsByTemplateNameAndCreatedBy(String templateName, Long createdBy);
}
