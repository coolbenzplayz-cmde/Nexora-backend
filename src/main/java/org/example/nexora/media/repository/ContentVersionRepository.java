package org.example.nexora.media.repository;

import org.example.nexora.media.entity.ContentVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContentVersionRepository extends JpaRepository<ContentVersion, Long> {
    
    List<ContentVersion> findByEditingJobIdOrderByVersionNumberDesc(Long editingJobId);
    
    Optional<ContentVersion> findByEditingJobIdAndVersionNumber(Long editingJobId, Integer versionNumber);
    
    List<ContentVersion> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    @Query("SELECT v FROM ContentVersion v WHERE v.editingJobId = :jobId AND v.isCurrent = true")
    Optional<ContentVersion> findCurrentVersion(@Param("jobId") Long jobId);
    
    @Query("SELECT MAX(v.versionNumber) FROM ContentVersion v WHERE v.editingJobId = :jobId")
    Integer findMaxVersionNumber(@Param("jobId") Long jobId);
    
    List<ContentVersion> findByEditingJobIdAndUserIdOrderByVersionNumberDesc(Long editingJobId, Long userId);
}
