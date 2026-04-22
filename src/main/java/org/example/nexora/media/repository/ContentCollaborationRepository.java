package org.example.nexora.media.repository;

import org.example.nexora.media.entity.ContentCollaboration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContentCollaborationRepository extends JpaRepository<ContentCollaboration, Long> {
    
    List<ContentCollaboration> findByEditingJobIdOrderByCreatedAtDesc(Long editingJobId);
    
    List<ContentCollaboration> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);
    
    List<ContentCollaboration> findByCollaboratorIdOrderByCreatedAtDesc(Long collaboratorId);
    
    List<ContentCollaboration> findByEditingJobIdAndStatusOrderByCreatedAtDesc(Long editingJobId, ContentCollaboration.CollaborationStatus status);
    
    Optional<ContentCollaboration> findByEditingJobIdAndCollaboratorId(Long editingJobId, Long collaboratorId);
    
    @Query("SELECT c FROM ContentCollaboration c WHERE c.collaboratorId = :userId AND c.status = 'PENDING'")
    List<ContentCollaboration> findPendingInvitations(@Param("userId") Long userId);
    
    @Query("SELECT c FROM ContentCollaboration c WHERE c.collaboratorId = :userId AND c.status = 'ACTIVE' AND c.expiresAt > :now")
    List<ContentCollaboration> findActiveCollaborations(@Param("userId") Long userId, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM ContentCollaboration c WHERE c.expiresAt < :now AND c.status IN ('PENDING', 'ACTIVE')")
    List<ContentCollaboration> findExpiredCollaborations(@Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(c) FROM ContentCollaboration c WHERE c.editingJobId = :jobId AND c.status = 'ACTIVE'")
    Long countActiveCollaborators(@Param("jobId") Long jobId);
}
