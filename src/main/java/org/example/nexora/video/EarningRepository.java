package org.example.nexora.video;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EarningRepository extends JpaRepository<Earning, Long> {

    List<Earning> findByUserId(Long userId);
}