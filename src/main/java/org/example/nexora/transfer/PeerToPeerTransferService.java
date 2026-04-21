package org.example.nexora.transfer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.transaction.Transaction;
import org.example.nexora.transaction.TransactionService;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.wallet.EnhancedWalletService;
import org.example.nexora.wallet.Wallet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PeerToPeerTransferService {

    private final PeerToPeerTransferRepository transferRepository;
    private final EnhancedWalletService walletService;
    private final UserRepository userRepository;
    private final TransactionService transactionService;

    private static final BigDecimal TRANSFER_FEE_RATE = new BigDecimal("0.005"); // 0.5% fee
    private static final BigDecimal MIN_TRANSFER_FEE = new BigDecimal("0.25");
    private static final BigDecimal MAX_TRANSFER_FEE = new BigDecimal("25.00");

    @Transactional
    public PeerToPeerTransfer createTransfer(Long senderId, Long receiverId, BigDecimal amount, String description) {
        return createTransfer(senderId, receiverId, amount, description, PeerToPeerTransfer.TransferType.INSTANT, null);
    }

    @Transactional
    public PeerToPeerTransfer createTransfer(Long senderId, Long receiverId, BigDecimal amount, String description,
                                           PeerToPeerTransfer.TransferType transferType, LocalDateTime scheduledAt) {
        // Validate inputs
        validateTransferRequest(senderId, receiverId, amount);

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new BusinessException("Sender not found"));
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new BusinessException("Receiver not found"));

        // Calculate fees
        BigDecimal feeAmount = calculateTransferFee(amount);
        BigDecimal totalAmount = amount.add(feeAmount);

        // Create transfer record
        PeerToPeerTransfer transfer = new PeerToPeerTransfer();
        transfer.setSenderId(senderId);
        transfer.setReceiverId(receiverId);
        transfer.setAmount(amount);
        transfer.setDescription(description);
        transfer.setTransferType(transferType);
        transfer.setScheduledAt(scheduledAt);
        transfer.setFeeAmount(feeAmount);
        transfer.setTotalAmount(totalAmount);

        // Set initial status
        if (transferType == PeerToPeerTransfer.TransferType.SCHEDULED && scheduledAt != null) {
            transfer.markAsScheduled();
        } else {
            transfer.markAsPending();
        }

        // Perform risk assessment
        performRiskAssessment(transfer);

        PeerToPeerTransfer savedTransfer = transferRepository.save(transfer);

        // Process immediately if it's an instant transfer
        if (transferType == PeerToPeerTransfer.TransferType.INSTANT) {
            processTransfer(savedTransfer);
        }

        log.info("Created {} transfer from {} to {} for {} (fee: {})", 
                transferType, senderId, receiverId, amount, feeAmount);

        return savedTransfer;
    }

    @Transactional
    public PeerToPeerTransfer createRecurringTransfer(Long senderId, Long receiverId, BigDecimal amount, 
                                                    String description, String interval, Integer maxCount) {
        PeerToPeerTransfer transfer = createTransfer(senderId, receiverId, amount, description, 
                PeerToPeerTransfer.TransferType.RECURRING, LocalDateTime.now().plusDays(1));
        
        transfer.setIsRecurring(true);
        transfer.setRecurringInterval(interval);
        transfer.setMaxRecurringCount(maxCount);
        transfer.setNextRecurringAt(calculateNextRecurringDate(interval, LocalDateTime.now()));
        
        return transferRepository.save(transfer);
    }

    @Transactional
    public PeerToPeerTransfer processTransfer(PeerToPeerTransfer transfer) {
        if (!transfer.canBeProcessed()) {
            throw new BusinessException("Transfer cannot be processed in current status: " + transfer.getStatus());
        }

        try {
            transfer.markAsProcessing();
            transferRepository.save(transfer);

            Wallet senderWallet = walletService.getWallet(transfer.getSenderId());
            Wallet receiverWallet = walletService.getWallet(transfer.getReceiverId());

            // Record balances before transfer
            transfer.setSenderBalanceBefore(senderWallet.getBalance());
            transfer.setReceiverBalanceBefore(receiverWallet.getBalance());

            // Check if sender can transfer
            if (!senderWallet.canTransfer(transfer.getTotalAmount())) {
                transfer.markAsFailed("Insufficient balance or limits exceeded");
                return transferRepository.save(transfer);
            }

            // Perform the transfer
            senderWallet.subtractBalance(transfer.getTotalAmount());
            senderWallet.recordTransferOut(transfer.getAmount());
            
            receiverWallet.addBalance(transfer.getAmount());
            receiverWallet.recordTransferIn(transfer.getAmount());

            // Save wallet updates
            walletService.save(senderWallet);
            walletService.save(receiverWallet);

            // Record balances after transfer
            transfer.setSenderBalanceAfter(senderWallet.getBalance());
            transfer.setReceiverBalanceAfter(receiverWallet.getBalance());

            // Create transaction record
            Transaction transaction = new Transaction();
            transaction.setReference(transfer.getReference());
            transaction.setFromUserId(transfer.getSenderId());
            transaction.setToUserId(transfer.getReceiverId());
            transaction.setAmount(transfer.getAmount());
            transaction.setStatus("COMPLETED");
            transaction.setDescription(transfer.getDescription() != null ? transfer.getDescription() : "P2P Transfer");
            transaction.setCreatedAt(LocalDateTime.now());
            transactionService.saveTransaction(transaction);

            transfer.markAsCompleted();
            
            // Schedule next recurring transfer if applicable
            if (transfer.isRecurringTransfer()) {
                transfer.scheduleNextRecurring();
            }

            PeerToPeerTransfer savedTransfer = transferRepository.save(transfer);

            log.info("Successfully processed transfer {} from {} to {} for {}", 
                    transfer.getReference(), transfer.getSenderId(), transfer.getReceiverId(), transfer.getAmount());

            return savedTransfer;

        } catch (Exception e) {
            log.error("Failed to process transfer {}: {}", transfer.getReference(), e.getMessage(), e);
            transfer.markAsFailed(e.getMessage());
            return transferRepository.save(transfer);
        }
    }

    @Transactional
    public void cancelTransfer(Long transferId, Long userId) {
        PeerToPeerTransfer transfer = getTransfer(transferId);
        
        // Only sender can cancel and only if transfer is pending or scheduled
        if (!transfer.getSenderId().equals(userId)) {
            throw new BusinessException("Only sender can cancel transfer");
        }
        
        if (!transfer.canBeProcessed()) {
            throw new BusinessException("Transfer cannot be cancelled in current status: " + transfer.getStatus());
        }

        transfer.markAsCancelled();
        transferRepository.save(transfer);

        log.info("Cancelled transfer {} by user {}", transferId, userId);
    }

    public PeerToPeerTransfer getTransfer(Long transferId) {
        return transferRepository.findById(transferId)
                .orElseThrow(() -> new BusinessException("Transfer not found: " + transferId));
    }

    public PeerToPeerTransfer getTransferByReference(String reference) {
        return transferRepository.findByReference(reference)
                .orElseThrow(() -> new BusinessException("Transfer not found with reference: " + reference));
    }

    public Page<PeerToPeerTransfer> getSentTransfers(Long userId, Pageable pageable) {
        return transferRepository.findBySenderIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<PeerToPeerTransfer> getReceivedTransfers(Long userId, Pageable pageable) {
        return transferRepository.findByReceiverIdOrderByCreatedAtDesc(userId, pageable);
    }

    public Page<PeerToPeerTransfer> getAllUserTransfers(Long userId, Pageable pageable) {
        return transferRepository.findBySenderIdOrReceiverIdOrderByCreatedAtDesc(userId, userId, pageable);
    }

    public List<PeerToPeerTransfer> getPendingTransfers() {
        return transferRepository.findByStatusOrderByCreatedAtAsc(PeerToPeerTransfer.TransferStatus.PENDING);
    }

    public List<PeerToPeerTransfer> getScheduledTransfers() {
        return transferRepository.findByStatusAndScheduledAtBeforeOrderByScheduledAtAsc(
                PeerToPeerTransfer.TransferStatus.SCHEDULED, LocalDateTime.now());
    }

    public List<PeerToPeerTransfer> getFlaggedTransfers() {
        return transferRepository.findByIsFlaggedTrueOrderByCreatedAtDesc();
    }

    public List<PeerToPeerTransfer> getRecurringTransfers() {
        return transferRepository.findByIsRecurringTrueAndNextRecurringAtBeforeOrderByNextRecurringAtAsc(LocalDateTime.now());
    }

    @Transactional
    @Scheduled(fixedRate = 60000) // Run every minute
    public void processScheduledTransfers() {
        List<PeerToPeerTransfer> scheduledTransfers = getScheduledTransfers();
        
        for (PeerToPeerTransfer transfer : scheduledTransfers) {
            try {
                processTransfer(transfer);
            } catch (Exception e) {
                log.error("Failed to process scheduled transfer {}: {}", transfer.getReference(), e.getMessage());
            }
        }
    }

    @Transactional
    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    public void processRecurringTransfers() {
        List<PeerToPeerTransfer> recurringTransfers = getRecurringTransfers();
        
        for (PeerToPeerTransfer transfer : recurringTransfers) {
            try {
                // Create new transfer instance for recurring payment
                PeerToPeerTransfer newTransfer = createTransfer(
                        transfer.getSenderId(),
                        transfer.getReceiverId(),
                        transfer.getAmount(),
                        "Recurring: " + transfer.getDescription(),
                        PeerToPeerTransfer.TransferType.INSTANT,
                        null
                );
                
                // Update original transfer's next recurring date
                transfer.scheduleNextRecurring();
                transferRepository.save(transfer);
                
            } catch (Exception e) {
                log.error("Failed to process recurring transfer {}: {}", transfer.getReference(), e.getMessage());
            }
        }
    }

    public TransferStatistics getTransferStatistics(Long userId) {
        List<PeerToPeerTransfer> sentTransfers = transferRepository.findBySenderId(userId);
        List<PeerToPeerTransfer> receivedTransfers = transferRepository.findByReceiverId(userId);

        BigDecimal totalSent = sentTransfers.stream()
                .filter(PeerToPeerTransfer::isCompleted)
                .map(PeerToPeerTransfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalReceived = receivedTransfers.stream()
                .filter(PeerToPeerTransfer::isCompleted)
                .map(PeerToPeerTransfer::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long completedSent = sentTransfers.stream()
                .filter(PeerToPeerTransfer::isCompleted)
                .count();

        long completedReceived = receivedTransfers.stream()
                .filter(PeerToPeerTransfer::isCompleted)
                .count();

        return TransferStatistics.builder()
                .totalSent(totalSent)
                .totalReceived(totalReceived)
                .netTransferred(totalReceived.subtract(totalSent))
                .completedTransfersSent(completedSent)
                .completedTransfersReceived(completedReceived)
                .pendingTransfers(sentTransfers.stream().filter(PeerToPeerTransfer::isPending).count())
                .build();
    }

    private void validateTransferRequest(Long senderId, Long receiverId, BigDecimal amount) {
        if (senderId.equals(receiverId)) {
            throw new BusinessException("Cannot transfer to yourself");
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Transfer amount must be positive");
        }

        if (amount.compareTo(new BigDecimal("100000")) > 0) {
            throw new BusinessException("Transfer amount exceeds maximum limit");
        }
    }

    private BigDecimal calculateTransferFee(BigDecimal amount) {
        BigDecimal fee = amount.multiply(TRANSFER_FEE_RATE);
        
        // Apply min/max fee limits
        if (fee.compareTo(MIN_TRANSFER_FEE) < 0) {
            fee = MIN_TRANSFER_FEE;
        } else if (fee.compareTo(MAX_TRANSFER_FEE) > 0) {
            fee = MAX_TRANSFER_FEE;
        }
        
        return fee;
    }

    private void performRiskAssessment(PeerToPeerTransfer transfer) {
        double riskScore = 0.0;

        // Amount-based risk
        if (transfer.getAmount().compareTo(new BigDecimal("1000")) > 0) {
            riskScore += 0.2;
        }
        if (transfer.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            riskScore += 0.3;
        }

        // New user risk (simplified - would check user age/activity)
        User sender = userRepository.findById(transfer.getSenderId()).orElse(null);
        if (sender != null && sender.getCreatedAt() != null) {
            long daysSinceCreation = java.time.Duration.between(sender.getCreatedAt(), LocalDateTime.now()).toDays();
            if (daysSinceCreation < 7) {
                riskScore += 0.3;
            } else if (daysSinceCreation < 30) {
                riskScore += 0.1;
            }
        }

        // Transfer frequency risk (simplified)
        long recentTransfers = transferRepository.countRecentTransfersBySenderId(transfer.getSenderId(), LocalDateTime.now().minusHours(24));
        if (recentTransfers > 10) {
            riskScore += 0.2;
        }

        transfer.setRiskScore(Math.min(riskScore, 1.0));
    }

    private LocalDateTime calculateNextRecurringDate(String interval, LocalDateTime from) {
        switch (interval.toUpperCase()) {
            case "DAILY":
                return from.plusDays(1);
            case "WEEKLY":
                return from.plusWeeks(1);
            case "MONTHLY":
                return from.plusMonths(1);
            default:
                return from.plusDays(1);
        }
    }
}
