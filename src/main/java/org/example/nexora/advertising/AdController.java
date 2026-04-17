package org.example.nexora.advertising;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/advertising")
public class AdController {

    @Autowired
    private AdService adService;

    @GetMapping("/ads")
    public ResponseEntity<Page<Ad>> getAllAds(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(adService.getAdsByStatus(Ad.AdStatus.ACTIVE, pageable));
    }

    @GetMapping("/ads/{id}")
    public ResponseEntity<Ad> getAdById(@PathVariable Long id) {
        return adService.getAdById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/ads/advertiser/{advertiserId}")
    public ResponseEntity<Page<Ad>> getAdsByAdvertiser(
            @PathVariable String advertiserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adService.getAdsByAdvertiser(advertiserId, pageable));
    }

    @GetMapping("/ads/status/{status}")
    public ResponseEntity<Page<Ad>> getAdsByStatus(
            @PathVariable Ad.AdStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(adService.getAdsByStatus(status, pageable));
    }

    @GetMapping("/ads/active")
    public ResponseEntity<List<Ad>> getActiveAds() {
        return ResponseEntity.ok(adService.getActiveAds());
    }

    @PostMapping("/ads")
    public ResponseEntity<Ad> createAd(@RequestBody Ad ad) {
        return ResponseEntity.ok(adService.createAd(ad));
    }

    @PutMapping("/ads/{id}")
    public ResponseEntity<Ad> updateAd(
            @PathVariable Long id, 
            @RequestBody Ad ad) {
        return ResponseEntity.ok(adService.updateAd(id, ad));
    }

    @PatchMapping("/ads/{id}/status")
    public ResponseEntity<Ad> updateAdStatus(
            @PathVariable Long id, 
            @RequestParam Ad.AdStatus status) {
        return ResponseEntity.ok(adService.updateAdStatus(id, status));
    }

    @PostMapping("/ads/{id}/impression")
    public ResponseEntity<Void> recordImpression(@PathVariable Long id) {
        adService.recordImpression(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/ads/{id}/click")
    public ResponseEntity<Void> recordClick(@PathVariable Long id) {
        adService.recordClick(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/ads/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        adService.deleteAd(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/stats/advertiser/{advertiserId}")
    public ResponseEntity<Map<String, Object>> getAdvertiserStats(@PathVariable String advertiserId) {
        Double totalSpend = adService.getTotalSpendByAdvertiser(advertiserId);
        Long totalImpressions = adService.getTotalImpressionsByAdvertiser(advertiserId);
        Long totalClicks = adService.getTotalClicksByAdvertiser(advertiserId);
        
        Double ctr = totalImpressions != null && totalImpressions > 0 
                ? (totalClicks * 100.0) / totalImpressions 
                : 0.0;
        
        return ResponseEntity.ok(Map.of(
                "totalSpend", totalSpend != null ? totalSpend : 0.0,
                "totalImpressions", totalImpressions != null ? totalImpressions : 0L,
                "totalClicks", totalClicks != null ? totalClicks : 0L,
                "clickThroughRate", ctr
        ));
    }
}
