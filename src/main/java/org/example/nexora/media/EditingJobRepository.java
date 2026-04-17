package org.example.nexora.media;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EditingJobRepository extends JpaRepository<EditingJob, Long> {

    List<EditingJob> findByUserIdOrderByCreatedAtDesc(Long userId);
}
