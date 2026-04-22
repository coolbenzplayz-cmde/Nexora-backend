package org.example.nexora.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Content version repository
 */
@Repository
public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {
    
    List<ContentVersion> findByContentIdOrderByVersionDesc(Long contentId);
    
    List<ContentVersion> findByContentIdAndCreatedByOrderByVersionDesc(Long contentId, Long createdBy);
    
    ContentVersion findByContentIdAndVersion(Long contentId, int version);
    
    boolean existsByContentIdAndVersion(Long contentId, int version);
}
