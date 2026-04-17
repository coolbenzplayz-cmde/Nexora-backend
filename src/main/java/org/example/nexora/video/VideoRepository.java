package org.example.nexora.video;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoRepository extends JpaRepository<Video, Long> {
    Page<Video> findByCreatorIdOrderByCreatedAtDesc(Long creatorId, Pageable pageable);
    Page<Video> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    Page<Video> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String title, Pageable pageable);
    List<Video> findByCreatorId(Long creatorId);
    long countByCreatorId(Long creatorId);
}
