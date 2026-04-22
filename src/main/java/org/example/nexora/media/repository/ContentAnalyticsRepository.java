package org.example.nexora.media.repository;

import org.example.nexora.media.entity.ContentAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ContentAnalyticsRepository extends JpaRepository<ContentAnalytics, Long> {
    
    List<ContentAnalytics> findByEditingJobIdOrderByTimestampDesc(Long editingJobId);
    
    List<ContentAnalytics> findByUserIdOrderByTimestampDesc(Long userId);
    
    List<ContentAnalytics> findByEditingJobIdAndEventTypeOrderByTimestampDesc(Long editingJobId, ContentAnalytics.AnalyticsEventType eventType);
    
    @Query("SELECT a FROM ContentAnalytics a WHERE a.editingJobId = :jobId AND a.timestamp >= :startDate ORDER BY a.timestamp DESC")
    List<ContentAnalytics> findByEditingJobIdAndTimestampAfterOrderByTimestampDesc(@Param("jobId") Long jobId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT COUNT(a) FROM ContentAnalytics a WHERE a.editingJobId = :jobId AND a.eventType = :eventType")
    Long countByEditingJobIdAndEventType(@Param("jobId") Long jobId, @Param("eventType") ContentAnalytics.AnalyticsEventType eventType);
    
    @Query("SELECT a FROM ContentAnalytics a WHERE a.userId = :userId AND a.timestamp >= :startDate ORDER BY a.timestamp DESC")
    List<ContentAnalytics> findByUserIdAndTimestampAfterOrderByTimestampDesc(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT a FROM ContentAnalytics a WHERE a.platform = :platform ORDER BY a.timestamp DESC")
    List<ContentAnalytics> findByPlatformOrderByTimestampDesc(@Param("platform") String platform);
    
    ContentAnalytics findTopByEditingJobIdOrderByTimestampDesc(Long editingJobId);
}
