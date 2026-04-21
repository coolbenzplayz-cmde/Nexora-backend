package org.example.nexora.monetization;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.BusinessException;
import org.example.nexora.user.User;
import org.example.nexora.user.UserRepository;
import org.example.nexora.user.UserRole;
import org.example.nexora.video.Video;
import org.example.nexora.video.VideoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MonetizationService {

    private final CreatorEarningsRepository creatorEarningsRepository;
    private final AdRevenueRepository adRevenueRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    // Default CPM rates (cost per 1000 impressions)
    private static final BigDecimal DEFAULT_CPM_RATE = new BigDecimal("2.50");
    private static final BigDecimal PREMIUM_CPM_RATE = new BigDecimal("5.00");
    private static final BigDecimal VERIFIED_CPM_RATE = new BigDecimal("8.00");

    /**
     * Calculate and record earnings from video views
     */
    @Transactional
    public CreatorEarnings calculateViewEarnings(Long videoId, long additionalViews) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException("Video not found"));

        User creator = userRepository.findById(video.getUserId())
                .orElseThrow(() -> new BusinessException("Creator not found"));

        if (creator.getRole() != UserRole.CREATOR) {
            throw new BusinessException("Only creators can earn from views");
        }

        // Determine CPM rate based on creator status
        BigDecimal cpmRate = determineCpmRate(creator);
        
        CreatorEarnings earnings = new CreatorEarnings();
        earnings.setCreatorId(video.getUserId());
        earnings.setVideoId(videoId);
        earnings.setEarningsType(CreatorEarnings.EarningsType.VIDEO_VIEWS);
        earnings.setEarningsDate(LocalDateTime.now());
        earnings.setDescription("Earnings from " + additionalViews + " views on video: " + video.getTitle());
        earnings.calculateEarningsFromViews(additionalViews, cpmRate);

        CreatorEarnings savedEarnings = creatorEarningsRepository.save(earnings);
        
        // Update creator's total earnings
        updateCreatorTotalEarnings(creator.getId());
        
        log.info("Recorded {} earnings for creator {} from {} views on video {}", 
                savedEarnings.getAmount(), creator.getId(), additionalViews, videoId);
        
        return savedEarnings;
    }

    /**
     * Calculate and record ad revenue
     */
    @Transactional
    public AdRevenue calculateAdRevenue(Long videoId, AdRevenue.AdType adType, 
                                       AdRevenue.AdPosition adPosition, 
                                       long impressions, long clicks) {
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new BusinessException("Video not found"));

        User creator = userRepository.findById(video.getUserId())
                .orElseThrow(() -> new BusinessException("Creator not found"));

        if (creator.getRole() != UserRole.CREATOR) {
            throw new BusinessException("Only creators can earn from ads");
        }

        // Determine ad rates based on ad type and creator status
        BigDecimal adRate = determineAdRate(adType, creator);
        
        AdRevenue adRevenue = new AdRevenue();
        adRevenue.setCreatorId(video.getUserId());
        adRevenue.setVideoId(videoId);
        adRevenue.setAdType(adType);
        adRevenue.setAdPosition(adPosition);
        adRevenue.setImpressions(impressions);
        adRevenue.setClicks(clicks);
        adRevenue.setRevenueDate(LocalDateTime.now());
        adRevenue.setStatus(AdRevenue.RevenueStatus.PENDING);

        // Calculate revenue based on ad type
        if (adType == AdRevenue.AdType.BANNER || adType == AdRevenue.AdType.OVERLAY) {
            adRevenue.calculateCpmRevenue(adRate);
        } else {
            adRevenue.calculateCpcRevenue(adRate);
        }

        AdRevenue savedAdRevenue = adRevenueRepository.save(adRevenue);
        
        log.info("Recorded {} ad revenue for creator {} from video {}", 
                savedAdRevenue.getRevenueAmount(), creator.getId(), videoId);
        
        return savedAdRevenue;
    }

    /**
     * Record bonus earnings for creators
     */
    @Transactional
    public CreatorEarnings recordBonusEarnings(Long creatorId, BigDecimal amount, String description) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException("Creator not found"));

        CreatorEarnings earnings = new CreatorEarnings();
        earnings.setCreatorId(creatorId);
        earnings.setEarningsType(CreatorEarnings.EarningsType.BONUS);
        earnings.setAmount(amount);
        earnings.setEarningsDate(LocalDateTime.now());
        earnings.setDescription(description);

        CreatorEarnings savedEarnings = creatorEarningsRepository.save(earnings);
        
        // Update creator's total earnings
        updateCreatorTotalEarnings(creatorId);
        
        log.info("Recorded bonus earnings of {} for creator {}: {}", amount, creatorId, description);
        
        return savedEarnings;
    }

    /**
     * Get creator's earnings summary
     */
    public CreatorEarningsSummary getCreatorEarningsSummary(Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new BusinessException("Creator not found"));

        List<CreatorEarnings> allEarnings = creatorEarningsRepository.findByCreatorIdOrderByEarningsDateDesc(creatorId);
        List<AdRevenue> allAdRevenue = adRevenueRepository.findByCreatorIdOrderByRevenueDateDesc(creatorId);

        BigDecimal totalEarnings = allEarnings.stream()
                .filter(e -> e.getEarningsType() != CreatorEarnings.EarningsType.REFUND)
                .map(CreatorEarnings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalAdRevenue = allAdRevenue.stream()
                .filter(ar -> ar.getStatus() == AdRevenue.RevenueStatus.VERIFIED)
                .map(AdRevenue::getRevenueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unpaidEarnings = allEarnings.stream()
                .filter(e -> !e.getIsPaid() && e.getEarningsType() != CreatorEarnings.EarningsType.REFUND)
                .map(CreatorEarnings::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal unpaidAdRevenue = allAdRevenue.stream()
                .filter(ar -> ar.isValidForPayment())
                .map(AdRevenue::getRevenueAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CreatorEarningsSummary.builder()
                .creatorId(creatorId)
                .creatorUsername(creator.getUsername())
                .totalEarnings(totalEarnings)
                .totalAdRevenue(totalAdRevenue)
                .unpaidEarnings(unpaidEarnings)
                .unpaidAdRevenue(unpaidAdRevenue)
                .totalUnpaid(unpaidEarnings.add(unpaidAdRevenue))
                .totalPaid(totalEarnings.subtract(unpaidEarnings))
                .earningsCount(allEarnings.size())
                .adRevenueCount(allAdRevenue.size())
                .build();
    }

    /**
     * Get creator's earnings history
     */
    public Page<CreatorEarnings> getCreatorEarningsHistory(Long creatorId, Pageable pageable) {
        return creatorEarningsRepository.findByCreatorIdOrderByEarningsDateDesc(creatorId, pageable);
    }

    /**
     * Get creator's ad revenue history
     */
    public Page<AdRevenue> getCreatorAdRevenueHistory(Long creatorId, Pageable pageable) {
        return adRevenueRepository.findByCreatorIdOrderByRevenueDateDesc(creatorId, pageable);
    }

    /**
     * Process pending ad revenue for payment
     */
    @Transactional
    public List<AdRevenue> processPendingAdRevenue(Long creatorId) {
        List<AdRevenue> pendingRevenue = adRevenueRepository.findByCreatorIdAndStatus(
                creatorId, AdRevenue.RevenueStatus.PENDING);

        for (AdRevenue revenue : pendingRevenue) {
            // Verify revenue (simple validation - in real system would be more complex)
            if (revenue.getRevenueAmount().compareTo(BigDecimal.ZERO) > 0) {
                revenue.markAsVerified();
                adRevenueRepository.save(revenue);
            }
        }

        return pendingRevenue;
    }

    /**
     * Get platform monetization statistics
     */
    public MonetizationStatistics getPlatformStatistics() {
        long totalCreators = userRepository.countByRole(UserRole.CREATOR);
        BigDecimal totalPlatformEarnings = creatorEarningsRepository.sumTotalEarnings() != null ? 
                creatorEarningsRepository.sumTotalEarnings() : BigDecimal.ZERO;
        BigDecimal totalPlatformAdRevenue = adRevenueRepository.sumTotalRevenue() != null ?
                adRevenueRepository.sumTotalRevenue() : BigDecimal.ZERO;

        return MonetizationStatistics.builder()
                .totalCreators(totalCreators)
                .totalEarnings(totalPlatformEarnings)
                .totalAdRevenue(totalPlatformAdRevenue)
                .totalRevenue(totalPlatformEarnings.add(totalPlatformAdRevenue))
                .build();
    }

    private BigDecimal determineCpmRate(User creator) {
        if (creator.getIsCreatorVerified()) {
            return VERIFIED_CPM_RATE;
        } else if (creator.getIsPremium()) {
            return PREMIUM_CPM_RATE;
        } else {
            return DEFAULT_CPM_RATE;
        }
    }

    private BigDecimal determineAdRate(AdRevenue.AdType adType, User creator) {
        BigDecimal baseRate = determineCpmRate(creator);
        
        // Adjust based on ad type
        switch (adType) {
            case PRE_ROLL:
                return baseRate.multiply(BigDecimal.valueOf(1.5));
            case MID_ROLL:
                return baseRate.multiply(BigDecimal.valueOf(1.2));
            case POST_ROLL:
                return baseRate.multiply(BigDecimal.valueOf(0.8));
            case BANNER:
                return baseRate.multiply(BigDecimal.valueOf(0.6));
            case OVERLAY:
                return baseRate.multiply(BigDecimal.valueOf(0.7));
            case SPONSORED_CONTENT:
                return baseRate.multiply(BigDecimal.valueOf(2.0));
            default:
                return baseRate;
        }
    }

    @Transactional
    private void updateCreatorTotalEarnings(Long creatorId) {
        User creator = userRepository.findById(creatorId).orElse(null);
        if (creator != null) {
            BigDecimal totalEarnings = creatorEarningsRepository.sumEarningsByCreatorId(creatorId);
            if (totalEarnings != null) {
                creator.setCreatorEarnings(totalEarnings.doubleValue());
                userRepository.save(creator);
            }
        }
    }
}
