package org.example.nexora.admin.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

/**
 * Response class for admin dashboard data
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    
    private Map<String, Object> systemOverview;
    private Map<String, Object> userStatistics;
    private Map<String, Object> serviceMetrics;
    private Map<String, Object> performanceData;
    private LocalDateTime generatedAt;
    
    public AdminDashboardResponse() {
        this.generatedAt = LocalDateTime.now();
    }
}
