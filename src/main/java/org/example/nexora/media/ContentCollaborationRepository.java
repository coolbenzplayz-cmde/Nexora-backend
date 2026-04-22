package org.example.nexora.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Content collaboration repository
 */
@Repository
public interface ContentCollaborationRepository extends JpaRepository<ContentCollaboration, Long> {
    
    List<ContentCollaboration> findByContentIdOrderByInvitedAtDesc(Long contentId);
    
    List<ContentCollaboration> findByCollaboratorIdOrderByInvitedAtDesc(Long collaboratorId);
    
    List<ContentCollaboration> findByContentIdAndCollaboratorId(Long contentId, Long collaboratorId);
    
    List<ContentCollaboration> findByStatusOrderByInvitedAtDesc(String status);
    
    void deleteByContentId(Long contentId);
}
