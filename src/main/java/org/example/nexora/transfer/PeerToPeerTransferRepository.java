package org.example.nexora.transfer;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface PeerToPeerTransferRepository extends JpaRepository<PeerToPeerTransfer, Long> {

    Optional<PeerToPeerTransfer> findByReference(String reference);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.senderId = :senderId ORDER BY t.createdAt DESC")
    Page<PeerToPeerTransfer> findBySenderIdOrderByCreatedAtDesc(@Param("senderId") Long senderId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.receiverId = :receiverId ORDER BY t.createdAt DESC")
    Page<PeerToPeerTransfer> findByReceiverIdOrderByCreatedAtDesc(@Param("receiverId") Long receiverId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.senderId = :senderId OR t.receiverId = :receiverId ORDER BY t.createdAt DESC")
    Page<PeerToPeerTransfer> findBySenderIdOrReceiverIdOrderByCreatedAtDesc(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, org.springframework.data.domain.Pageable pageable);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.senderId = :senderId")
    List<PeerToPeerTransfer> findBySenderId(@Param("senderId") Long senderId);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.receiverId = :receiverId")
    List<PeerToPeerTransfer> findByReceiverId(@Param("receiverId") Long receiverId);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.status = :status ORDER BY t.createdAt ASC")
    List<PeerToPeerTransfer> findByStatusOrderByCreatedAtAsc(@Param("status") PeerToPeerTransfer.TransferStatus status);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.status = :status AND t.scheduledAt <= :scheduledAt ORDER BY t.scheduledAt ASC")
    List<PeerToPeerTransfer> findByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(@Param("status") PeerToPeerTransfer.TransferStatus status, @Param("scheduledAt") LocalDateTime scheduledAt);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.isFlagged = true ORDER BY t.createdAt DESC")
    List<PeerToPeerTransfer> findByIsFlaggedTrueOrderByCreatedAtDesc();

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.isRecurring = true AND t.nextRecurringAt <= :nextRecurringAt ORDER BY t.nextRecurringAt ASC")
    List<PeerToPeerTransfer> findByIsRecurringTrueAndNextRecurringAtBeforeOrderByNextRecurringAtAsc(@Param("nextRecurringAt") LocalDateTime nextRecurringAt);

    @Query("SELECT COUNT(t) FROM PeerToPeerTransfer t WHERE t.senderId = :senderId AND t.createdAt >= :since")
    long countRecentTransfersBySenderId(@Param("senderId") Long senderId, @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(t) FROM PeerToPeerTransfer t WHERE t.receiverId = :receiverId AND t.createdAt >= :since")
    long countRecentTransfersByReceiverId(@Param("receiverId") Long receiverId, @Param("since") LocalDateTime since);

    @Query("SELECT SUM(t.amount) FROM PeerToPeerTransfer t WHERE t.senderId = :senderId AND t.status = 'COMPLETED'")
    BigDecimal sumTotalAmountSentByUserId(@Param("senderId") Long senderId);

    @Query("SELECT SUM(t.amount) FROM PeerToPeerTransfer t WHERE t.receiverId = :receiverId AND t.status = 'COMPLETED'")
    BigDecimal sumTotalAmountReceivedByUserId(@Param("receiverId") Long receiverId);

    @Query("SELECT SUM(t.feeAmount) FROM PeerToPeerTransfer t WHERE t.status = 'COMPLETED'")
    BigDecimal sumTotalFeesCollected();

    @Query("SELECT COUNT(t) FROM PeerToPeerTransfer t WHERE t.status = :status")
    long countByStatus(@Param("status") PeerToPeerTransfer.TransferStatus status);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
    List<PeerToPeerTransfer> findByCreatedAtBetween(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.amount >= :minAmount ORDER BY t.amount DESC")
    List<PeerToPeerTransfer> findTransfersWithAmountGreaterThanEqual(@Param("minAmount") BigDecimal minAmount);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.riskScore > :minRiskScore ORDER BY t.riskScore DESC")
    List<PeerToPeerTransfer> findHighRiskTransfers(@Param("minRiskScore") Double minRiskScore);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.transferType = :transferType ORDER BY t.createdAt DESC")
    List<PeerToPeerTransfer> findByTransferType(@Param("transferType") PeerToPeerTransfer.TransferType transferType);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.isRecurring = true ORDER BY t.createdAt DESC")
    List<PeerToPeerTransfer> findRecurringTransfers();

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.senderId = :senderId AND t.receiverId = :receiverId ORDER BY t.createdAt DESC")
    List<PeerToPeerTransfer> findTransfersBetweenUsers(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);

    @Query("SELECT COUNT(t) FROM PeerToPeerTransfer t WHERE t.createdAt >= :since")
    long countTransfersSince(@Param("since") LocalDateTime since);

    @Query("SELECT SUM(t.amount) FROM PeerToPeerTransfer t WHERE t.createdAt >= :since AND t.status = 'COMPLETED'")
    BigDecimal sumTransferVolumeSince(@Param("since") LocalDateTime since);

    @Query("SELECT t FROM PeerToPeerTransfer t WHERE t.status = 'PENDING' AND t.createdAt <= :cutoffTime")
    List<PeerToPeerTransfer> findStalePendingTransfers(@Param("cutoffTime") LocalDateTime cutoffTime);
}
