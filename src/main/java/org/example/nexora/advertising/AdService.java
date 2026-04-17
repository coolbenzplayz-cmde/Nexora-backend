package org.example.nexora.advertising;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AdService {

    @Autowired
    private AdRepository adRepository;

    public Ad createAd(Ad ad) {
        ad.setStatus(Ad.AdStatus.DRAFT);
        return adRepository.save(ad);
    }

    public Optional<Ad> getAdById(Long id) {
        return adRepository.findById(id);
    }

    public Page<Ad> getAdsByAdvertiser(String advertiserId, Pageable pageable) {
        return adRepository.findByAdvertiserId(advertiserId, pageable);
    }

    public Page<Ad> getAdsByStatus(Ad.AdStatus status, Pageable pageable) {
        return adRepository.findByStatus(status, pageable);
    }

    public List<Ad> getActiveAds() {
        return adRepository.findActiveAds();
    }

    public Page<Ad> getAdsByAdvertiserAndStatus(String advertiserId, Ad.AdStatus status, Pageable pageable) {
        return adRepository.findByAdvertiserAndStatus(advertiserId, status, pageable);
    }

    public Ad updateAd(Long id, Ad adDetails) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        ad.setTitle(adDetails.getTitle());
        ad.setDescription(adDetails.getDescription());
        ad.setImageUrl(adDetails.getImageUrl());
        ad.setTargetUrl(adDetails.getTargetUrl());
        ad.setFormat(adDetails.getFormat());
        ad.setTargetAudience(adDetails.getTargetAudience());
        ad.setBudget(adDetails.getBudget());
        ad.setCostPerClick(adDetails.getCostPerClick());
        ad.setCostPerImpression(adDetails.getCostPerImpression());
        ad.setStartDate(adDetails.getStartDate());
        ad.setEndDate(adDetails.getEndDate());
        ad.setDailyBudget(adDetails.getDailyBudget());
        ad.setReachLimit(adDetails.getReachLimit());

        return adRepository.save(ad);
    }

    public Ad updateAdStatus(Long id, Ad.AdStatus status) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));

        ad.setStatus(status);
        
        if (status == Ad.AdStatus.ACTIVE && ad.getStartDate() == null) {
            ad.setStartDate(LocalDateTime.now());
        }
        
        return adRepository.save(ad);
    }

    public void recordImpression(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        
        ad.incrementImpressions();
        
        if (ad.getCostPerImpression() != null) {
            BigDecimal increment = ad.getCostPerImpression();
            ad.setSpent(ad.getSpent() == null ? increment : ad.getSpent().add(increment));
        }
        
        adRepository.save(ad);
    }

    public void recordClick(Long id) {
        Ad ad = adRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ad not found"));
        
        ad.incrementClicks();
        
        if (ad.getCostPerClick() != null) {
            BigDecimal increment = ad.getCostPerClick();
            ad.setSpent(ad.getSpent() == null ? increment : ad.getSpent().add(increment));
        }
        
        adRepository.save(ad);
    }

    public void deleteAd(Long id) {
        adRepository.deleteById(id);
    }

    public Double getTotalSpendByAdvertiser(String advertiserId) {
        return adRepository.calculateTotalSpendByAdvertiser(advertiserId);
    }

    public Long getTotalImpressionsByAdvertiser(String advertiserId) {
        return adRepository.calculateTotalImpressionsByAdvertiser(advertiserId);
    }

    public Long getTotalClicksByAdvertiser(String advertiserId) {
        return adRepository.calculateTotalClicksByAdvertiser(advertiserId);
    }
}
