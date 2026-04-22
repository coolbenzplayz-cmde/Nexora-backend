package org.example.nexora.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Content analytics repository
 */
@Repository
public interface ContentAnalyticsRepository extends JpaRepository<ContentAnalytics, Long> {
    
    List<ContentAnalytics> findByContentIdOrderByTimestampDesc(Long contentId);
    
    List<ContentAnalytics> findByContentIdAndTimestampBetweenOrderByTimestampDesc(Long contentId, LocalDateTime startTime, LocalDateTime endTime);
    
    List<ContentAnalytics> findByEventTypeOrderByTimestampDesc(String eventType);
    
    void deleteByContentId(Long contentId);
}
