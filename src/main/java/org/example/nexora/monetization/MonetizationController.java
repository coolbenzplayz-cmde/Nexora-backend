package org.example.nexora.monetization;

import lombok.RequiredArgsConstructor;
import org.example.nexora.common.ApiResponse;
import org.example.nexora.common.PaginationResponse;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/monetization")
@RequiredArgsConstructor
public class MonetizationController {

    private final MonetizationService monetizationService;

    @PostMapping("/earnings/views/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreatorEarnings>> calculateViewEarnings(
            @PathVariable Long videoId,
            @Valid @RequestBody ViewEarningsRequest request) {
        
        CreatorEarnings earnings = monetizationService.calculateViewEarnings(videoId, request.getAdditionalViews());
        return ResponseEntity.ok(ApiResponse.success(earnings, "View earnings calculated successfully"));
    }

    @PostMapping("/earnings/ads/{videoId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AdRevenue>> calculateAdRevenue(
            @PathVariable Long videoId,
            @Valid @RequestBody AdRevenueRequest request) {
        
        AdRevenue adRevenue = monetizationService.calculateAdRevenue(
                videoId, request.getAdType(), request.getAdPosition(), 
                request.getImpressions(), request.getClicks());
        return ResponseEntity.ok(ApiResponse.success(adRevenue, "Ad revenue calculated successfully"));
    }

    @PostMapping("/earnings/bonus/{creatorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CreatorEarnings>> recordBonusEarnings(
            @PathVariable Long creatorId,
            @Valid @RequestBody BonusEarningsRequest request) {
        
        CreatorEarnings earnings = monetizationService.recordBonusEarnings(
                creatorId, request.getAmount(), request.getDescription());
        return ResponseEntity.ok(ApiResponse.success(earnings, "Bonus earnings recorded successfully"));
    }

    @GetMapping("/summary/{creatorId}")
    public ResponseEntity<ApiResponse<CreatorEarningsSummary>> getCreatorEarningsSummary(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long creatorId) {
        
        // Users can only see their own summary unless they're admins
        if (!currentUser.getId().equals(creatorId) && currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("You can only view your own earnings summary");
        }
        
        CreatorEarningsSummary summary = monetizationService.getCreatorEarningsSummary(creatorId);
        return ResponseEntity.ok(ApiResponse.success(summary, "Earnings summary retrieved successfully"));
    }

    @GetMapping("/earnings/history/{creatorId}")
    public ResponseEntity<ApiResponse<PaginationResponse<CreatorEarnings>>> getCreatorEarningsHistory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!currentUser.getId().equals(creatorId) && currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("You can only view your own earnings history");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CreatorEarnings> earnings = monetizationService.getCreatorEarningsHistory(creatorId, pageable);
        
        PaginationResponse<CreatorEarnings> response = PaginationResponse.<CreatorEarnings>builder()
                .content(earnings.getContent())
                .page(earnings.getNumber())
                .size(earnings.getSize())
                .totalElements(earnings.getTotalElements())
                .totalPages(earnings.getTotalPages())
                .first(earnings.isFirst())
                .last(earnings.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ads/history/{creatorId}")
    public ResponseEntity<ApiResponse<PaginationResponse<AdRevenue>>> getCreatorAdRevenueHistory(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Long creatorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (!currentUser.getId().equals(creatorId) && currentUser.getRole() != UserRole.ADMIN) {
            throw new RuntimeException("You can only view your own ad revenue history");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdRevenue> adRevenue = monetizationService.getCreatorAdRevenueHistory(creatorId, pageable);
        
        PaginationResponse<AdRevenue> response = PaginationResponse.<AdRevenue>builder()
                .content(adRevenue.getContent())
                .page(adRevenue.getNumber())
                .size(adRevenue.getSize())
                .totalElements(adRevenue.getTotalElements())
                .totalPages(adRevenue.getTotalPages())
                .first(adRevenue.isFirst())
                .last(adRevenue.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/ads/process-pending/{creatorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AdRevenue>>> processPendingAdRevenue(@PathVariable Long creatorId) {
        List<AdRevenue> processedRevenue = monetizationService.processPendingAdRevenue(creatorId);
        return ResponseEntity.ok(ApiResponse.success(processedRevenue, "Pending ad revenue processed successfully"));
    }

    @GetMapping("/statistics/platform")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<MonetizationStatistics>> getPlatformStatistics() {
        MonetizationStatistics stats = monetizationService.getPlatformStatistics();
        return ResponseEntity.ok(ApiResponse.success(stats, "Platform statistics retrieved successfully"));
    }

    @GetMapping("/earnings/my-summary")
    public ResponseEntity<ApiResponse<CreatorEarningsSummary>> getMyEarningsSummary(
            @AuthenticationPrincipal User currentUser) {
        
        if (currentUser.getRole() != UserRole.CREATOR) {
            throw new RuntimeException("Only creators can view earnings summary");
        }
        
        CreatorEarningsSummary summary = monetizationService.getCreatorEarningsSummary(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(summary, "Your earnings summary retrieved successfully"));
    }

    @GetMapping("/earnings/my-history")
    public ResponseEntity<ApiResponse<PaginationResponse<CreatorEarnings>>> getMyEarningsHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (currentUser.getRole() != UserRole.CREATOR) {
            throw new RuntimeException("Only creators can view earnings history");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<CreatorEarnings> earnings = monetizationService.getCreatorEarningsHistory(currentUser.getId(), pageable);
        
        PaginationResponse<CreatorEarnings> response = PaginationResponse.<CreatorEarnings>builder()
                .content(earnings.getContent())
                .page(earnings.getNumber())
                .size(earnings.getSize())
                .totalElements(earnings.getTotalElements())
                .totalPages(earnings.getTotalPages())
                .first(earnings.isFirst())
                .last(earnings.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ads/my-history")
    public ResponseEntity<ApiResponse<PaginationResponse<AdRevenue>>> getMyAdRevenueHistory(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        if (currentUser.getRole() != UserRole.CREATOR) {
            throw new RuntimeException("Only creators can view ad revenue history");
        }
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AdRevenue> adRevenue = monetizationService.getCreatorAdRevenueHistory(currentUser.getId(), pageable);
        
        PaginationResponse<AdRevenue> response = PaginationResponse.<AdRevenue>builder()
                .content(adRevenue.getContent())
                .page(adRevenue.getNumber())
                .size(adRevenue.getSize())
                .totalElements(adRevenue.getTotalElements())
                .totalPages(adRevenue.getTotalPages())
                .first(adRevenue.isFirst())
                .last(adRevenue.isLast())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
