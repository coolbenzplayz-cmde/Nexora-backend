package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.example.nexora.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Concurrency monitoring controller for system health and performance
 */
@RestController
@RequestMapping("/api/concurrency")
@Slf4j
public class ConcurrencyMonitorController {

    private final ConcurrencyGuard concurrencyGuard;
    private final AsyncTaskProcessor asyncTaskProcessor;
    private final CircuitBreakerManager circuitBreakerManager;
    private final RateLimiter rateLimiter;
    private final HealthMonitor healthMonitor;
    private final ResourceManager resourceManager;

    public ConcurrencyMonitorController(
            ConcurrencyGuard concurrencyGuard,
            AsyncTaskProcessor asyncTaskProcessor,
            CircuitBreakerManager circuitBreakerManager,
            RateLimiter rateLimiter,
            HealthMonitor healthMonitor,
            ResourceManager resourceManager) {
        this.concurrencyGuard = concurrencyGuard;
        this.asyncTaskProcessor = asyncTaskProcessor;
        this.circuitBreakerManager = circuitBreakerManager;
        this.rateLimiter = rateLimiter;
        this.healthMonitor = healthMonitor;
        this.resourceManager = resourceManager;
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ConcurrencyGuard.SystemStatus>> getSystemStatus() {
        ConcurrencyGuard.SystemStatus status = concurrencyGuard.getSystemStatus();
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/health")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<HealthMonitor.SystemHealth>> getSystemHealth() {
        HealthMonitor.SystemHealth health = healthMonitor.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.success(health));
    }

    @GetMapping("/tasks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AsyncTaskProcessor.TaskStats>> getTaskStats() {
        AsyncTaskProcessor.TaskStats stats = asyncTaskProcessor.getStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/resources")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<ResourceManager.SystemResourceStats>> getResourceStats() {
        ResourceManager.SystemResourceStats stats = resourceManager.getSystemResourceStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/circuit-breakers")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getCircuitBreakerStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalCircuitBreakers", circuitBreakerManager.circuitBreakers.size());
        
        Map<String, String> states = new HashMap<>();
        circuitBreakerManager.circuitBreakers.forEach((name, cb) -> {
            states.put(name, cb.getState().toString());
        });
        stats.put("states", states);
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/rate-limits")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, RateLimiter.RateLimitStatus>>> getRateLimitStats(
            @RequestParam(required = false) String key) {
        
        Map<String, RateLimiter.RateLimitStatus> stats = new HashMap<>();
        
        if (key != null) {
            RateLimiter.RateLimitStatus status = rateLimiter.getStatus(key);
            stats.put(key, status);
        } else {
            // Return some default rate limiters
            stats.put("media-service", rateLimiter.getStatus("media-service"));
            stats.put("auth-service", rateLimiter.getStatus("auth-service"));
            stats.put("user-service", rateLimiter.getStatus("user-service"));
        }
        
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @PostMapping("/cleanup")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> performCleanup() {
        concurrencyGuard.performCleanup();
        return ResponseEntity.ok(ApiResponse.success("Cleanup completed successfully"));
    }

    @PostMapping("/reset-circuit-breaker/{serviceName}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetCircuitBreaker(@PathVariable String serviceName) {
        circuitBreakerManager.getCircuitBreaker(serviceName).reset();
        return ResponseEntity.ok(ApiResponse.success("Circuit breaker reset for service: " + serviceName));
    }

    @PostMapping("/reset-rate-limit/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> resetRateLimit(@PathVariable String key) {
        rateLimiter.resetBucket(key);
        return ResponseEntity.ok(ApiResponse.success("Rate limit reset for key: " + key));
    }

    @GetMapping("/metrics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDetailedMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // System health metrics
        HealthMonitor.SystemHealth health = healthMonitor.getSystemHealth();
        metrics.put("systemHealth", Map.of(
            "memoryUsage", health.getMemoryUsage(),
            "threadCount", health.getThreadCount(),
            "cpuUsage", health.getCpuUsage(),
            "alertCount", health.getAlertCount()
        ));
        
        // Task metrics
        AsyncTaskProcessor.TaskStats taskStats = asyncTaskProcessor.getStats();
        metrics.put("taskMetrics", Map.of(
            "activeTasks", taskStats.getActiveTasks(),
            "completedTasks", taskStats.getCompletedTasks(),
            "failedTasks", taskStats.getFailedTasks()
        ));
        
        // Resource metrics
        ResourceManager.SystemResourceStats resourceStats = resourceManager.getSystemResourceStats();
        metrics.put("resourceMetrics", Map.of(
            "totalAllocatedMemory", resourceStats.getTotalAllocatedMemory(),
            "totalOpenFiles", resourceStats.getTotalOpenFiles(),
            "totalActiveConnections", resourceStats.getTotalActiveConnections(),
            "activePools", resourceStats.getActivePools()
        ));
        
        return ResponseEntity.ok(ApiResponse.success(metrics));
    }

    @GetMapping("/performance")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPerformanceMetrics() {
        Map<String, Object> performance = new HashMap<>();
        
        // Calculate performance indicators
        AsyncTaskProcessor.TaskStats taskStats = asyncTaskProcessor.getStats();
        int totalTasks = taskStats.getCompletedTasks() + taskStats.getFailedTasks();
        double successRate = totalTasks > 0 ? (double) taskStats.getCompletedTasks() / totalTasks : 0.0;
        double failureRate = totalTasks > 0 ? (double) taskStats.getFailedTasks() / totalTasks : 0.0;
        
        performance.put("successRate", successRate * 100);
        performance.put("failureRate", failureRate * 100);
        performance.put("totalTasksProcessed", totalTasks);
        performance.put("currentActiveTasks", taskStats.getActiveTasks());
        
        // System load indicators
        HealthMonitor.SystemHealth health = healthMonitor.getSystemHealth();
        performance.put("memoryLoad", health.getMemoryUsage() * 100);
        performance.put("threadLoad", (double) health.getThreadCount() / 500 * 100); // Assuming 500 as max thread threshold
        
        // Overall system performance score (0-100)
        double performanceScore = calculatePerformanceScore(successRate, health.getMemoryUsage(), health.getThreadCount());
        performance.put("performanceScore", performanceScore);
        performance.put("status", getPerformanceStatus(performanceScore));
        
        return ResponseEntity.ok(ApiResponse.success(performance));
    }

    private double calculatePerformanceScore(double successRate, double memoryUsage, int threadCount) {
        // Success rate weight: 40%
        double successScore = successRate * 0.4;
        
        // Memory usage weight: 30% (lower is better)
        double memoryScore = (1.0 - Math.min(memoryUsage, 1.0)) * 0.3;
        
        // Thread usage weight: 30% (lower is better)
        double threadScore = Math.max(0, 1.0 - (double) threadCount / 500) * 0.3;
        
        return (successScore + memoryScore + threadScore) * 100;
    }

    private String getPerformanceStatus(double score) {
        if (score >= 90) return "EXCELLENT";
        if (score >= 75) return "GOOD";
        if (score >= 60) return "FAIR";
        if (score >= 40) return "POOR";
        return "CRITICAL";
    }

    @PostMapping("/emergency-shutdown")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> emergencyShutdown() {
        concurrencyGuard.emergencyShutdown();
        return ResponseEntity.ok(ApiResponse.success("Emergency shutdown initiated"));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getDashboardData() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Quick stats
        dashboard.put("systemStatus", concurrencyGuard.getSystemStatus());
        dashboard.put("systemHealth", healthMonitor.getSystemHealth());
        dashboard.put("taskStats", asyncTaskProcessor.getStats());
        dashboard.put("resourceStats", resourceManager.getSystemResourceStats());
        
        // Performance summary
        Map<String, Object> performance = getPerformanceMetrics().getBody().getData();
        dashboard.put("performance", performance);
        
        // Alerts and warnings
        Map<String, Object> alerts = new HashMap<>();
        HealthMonitor.SystemHealth health = healthMonitor.getSystemHealth();
        
        if (health.getMemoryUsage() > 0.8) {
            alerts.put("memory", "High memory usage detected");
        }
        if (health.getThreadCount() > 400) {
            alerts.put("threads", "High thread count detected");
        }
        if (health.getAlertCount() > 10) {
            alerts.put("alerts", "High alert count detected");
        }
        
        dashboard.put("alerts", alerts);
        
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }
}
