package org.example.nexora.wallet;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WithdrawRepository extends JpaRepository<WithdrawRequest, Long> {

    List<WithdrawRequest> findByUserId(Long userId);
}