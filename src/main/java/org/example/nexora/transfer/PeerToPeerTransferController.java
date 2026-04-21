package org.example.nexora.transfer;

import lombok.RequiredArgsConstructor;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.example.nexora.transfer.dto.SendMoneyRequest;
import org.example.nexora.transfer.dto.ScheduleTransferRequest;
import org.example.nexora.transfer.dto.RecurringTransferRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/transfers")
@RequiredArgsConstructor
public class PeerToPeerTransferController {

    private final PeerToPeerTransferService transferService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<PeerToPeerTransfer>> sendMoney(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody SendMoneyRequest request) {
        
        PeerToPeerTransfer transfer = transferService.createTransfer(
                currentUser.getId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getDescription()
        );
        
        return ResponseEntity.ok(ApiResponse.success(transfer, "Transfer initiated successfully"));
    }

    @PostMapping("/schedule")
    public ResponseEntity<ApiResponse<PeerToPeerTransfer>> scheduleTransfer(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody ScheduleTransferRequest request) {
        
        PeerToPeerTransfer transfer = transferService.createTransfer(
                currentUser.getId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getDescription(),
                PeerToPeerTransfer.TransferType.SCHEDULED,
                request.getScheduledAt()
        );
        
        return ResponseEntity.ok(ApiResponse.success(transfer, "Transfer scheduled successfully"));
    }

    @PostMapping("/recurring")
    public ResponseEntity<ApiResponse<PeerToPeerTransfer>> createRecurringTransfer(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody RecurringTransferRequest request) {
        
        PeerToPeerTransfer transfer = transferService.createRecurringTransfer(
                currentUser.getId(),
                request.getReceiverId(),
                request.getAmount(),
                request.getDescription(),
                request.getInterval(),
                request.getMaxCount()
        );
        
        return ResponseEntity.ok(ApiResponse.success(transfer, "Recurring transfer created successfully"));
    }

    @PostMapping("/cancel/{transferId}")
    public ResponseEntity<ApiResponse<Void>> cancelTransfer(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long transferId) {
        
        transferService.cancelTransfer(transferId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(null, "Transfer cancelled successfully"));
    }

    @GetMapping("/{transferId}")
    public ResponseEntity<ApiResponse<PeerToPeerTransfer>> getTransfer(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long transferId) {
        
        PeerToPeerTransfer transfer = transferService.getTransfer(transferId);
        
        // Validate user can view this transfer
        if (!transfer.getSenderId().equals(currentUser.getId()) && 
            !transfer.getReceiverId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only view your own transfers");
        }
        
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    @GetMapping("/reference/{reference}")
    public ResponseEntity<ApiResponse<PeerToPeerTransfer>> getTransferByReference(
            @AuthenticationPrincipal User currentUser,
            @PathVariable String reference) {
        
        PeerToPeerTransfer transfer = transferService.getTransferByReference(reference);
        
        // Validate user can view this transfer
        if (!transfer.getSenderId().equals(currentUser.getId()) && 
            !transfer.getReceiverId().equals(currentUser.getId())) {
            throw new RuntimeException("You can only view your own transfers");
        }
        
        return ResponseEntity.ok(ApiResponse.success(transfer));
    }

    @GetMapping("/sent")
    public ResponseEntity<ApiResponse<PaginationResponse<PeerToPeerTransfer>>> getSentTransfers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PeerToPeerTransfer> transfers = transferService.getSentTransfers(currentUser.getId(), pageable);
        
        PaginationResponse<PeerToPeerTransfer> response = PaginationResponse.<PeerToPeerTransfer>builder()
                .content(transfers.getContent())
                .page(transfers.getNumber())
                .size(transfers.getSize())
                .totalElements(transfers.getTotalElements())
                .totalPages(transfers.getTotalPages())
                .first(transfers.isFirst())
                .last(transfers.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/received")
    public ResponseEntity<ApiResponse<PaginationResponse<PeerToPeerTransfer>>> getReceivedTransfers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PeerToPeerTransfer> transfers = transferService.getReceivedTransfers(currentUser.getId(), pageable);
        
        PaginationResponse<PeerToPeerTransfer> response = PaginationResponse.<PeerToPeerTransfer>builder()
                .content(transfers.getContent())
                .page(transfers.getNumber())
                .size(transfers.getSize())
                .totalElements(transfers.getTotalElements())
                .totalPages(transfers.getTotalPages())
                .first(transfers.isFirst())
                .last(transfers.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<PaginationResponse<PeerToPeerTransfer>>> getAllTransfers(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<PeerToPeerTransfer> transfers = transferService.getAllUserTransfers(currentUser.getId(), pageable);
        
        PaginationResponse<PeerToPeerTransfer> response = PaginationResponse.<PeerToPeerTransfer>builder()
                .content(transfers.getContent())
                .page(transfers.getNumber())
                .size(transfers.getSize())
                .totalElements(transfers.getTotalElements())
                .totalPages(transfers.getTotalPages())
                .first(transfers.isFirst())
                .last(transfers.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<TransferStatistics>> getTransferStatistics(
            @AuthenticationPrincipal User currentUser) {
        
        TransferStatistics stats = transferService.getTransferStatistics(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PeerToPeerTransfer>>> getPendingTransfers(
            @AuthenticationPrincipal User currentUser) {
        
        List<PeerToPeerTransfer> pendingTransfers = transferService.getPendingTransfers();
        
        // Filter to only show user's pending transfers
        List<PeerToPeerTransfer> userPendingTransfers = pendingTransfers.stream()
                .filter(t -> t.getSenderId().equals(currentUser.getId()) || t.getReceiverId().equals(currentUser.getId()))
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(userPendingTransfers));
    }

    @GetMapping("/recurring")
    public ResponseEntity<ApiResponse<List<PeerToPeerTransfer>>> getRecurringTransfers(
            @AuthenticationPrincipal User currentUser) {
        
        List<PeerToPeerTransfer> recurringTransfers = transferService.getRecurringTransfers();
        
        // Filter to only show user's recurring transfers
        List<PeerToPeerTransfer> userRecurringTransfers = recurringTransfers.stream()
                .filter(t -> t.getSenderId().equals(currentUser.getId()))
                .toList();
        
        return ResponseEntity.ok(ApiResponse.success(userRecurringTransfers));
    }
}
