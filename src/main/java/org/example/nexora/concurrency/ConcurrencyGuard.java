package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Central concurrency guard that coordinates all concurrency components
 * for crash prevention and safe multitasking
 */
@Component
@Slf4j
public class ConcurrencyGuard {

    private final AsyncTaskProcessor asyncTaskProcessor;
    private final CircuitBreakerManager circuitBreakerManager;
    private final RateLimiter rateLimiter;
    private final HealthMonitor healthMonitor;
    private final ResourceManager resourceManager;

    public ConcurrencyGuard(
            AsyncTaskProcessor asyncTaskProcessor,
            CircuitBreakerManager circuitBreakerManager,
            RateLimiter rateLimiter,
            HealthMonitor healthMonitor,
            ResourceManager resourceManager) {
        this.asyncTaskProcessor = asyncTaskProcessor;
        this.circuitBreakerManager = circuitBreakerManager;
        this.rateLimiter = rateLimiter;
        this.healthMonitor = healthMonitor;
        this.resourceManager = resourceManager;
    }

    /**
     * Execute a task with full concurrency protection
     */
    public <T> CompletableFuture<T> executeSafely(
            String taskName,
            String serviceName,
            Supplier<T> task,
            Supplier<T> fallback,
            TaskConfig config) {
        
        // Check rate limiting
        if (!rateLimiter.allowRequest(serviceName, config.getRateLimit(), config.getRateWindowSeconds())) {
            log.warn("Rate limit exceeded for service: {}", serviceName);
            return CompletableFuture.completedFuture(fallback.get());
        }

        // Execute with circuit breaker
        return CompletableFuture.supplyAsync(() -> {
            return circuitBreakerManager.executeWithCircuitBreaker(
                serviceName,
                () -> {
                    // Record health metrics
                    healthMonitor.recordMetric("task." + taskName + ".started", 1);
                    
                    try {
                        // Execute with timeout
                        CompletableFuture<T> future = asyncTaskProcessor.submitWithTimeout(
                            () -> AsyncTaskProcessor.TaskResult.success(task.get().toString()),
                            config.getTimeoutSeconds(),
                            java.util.concurrent.TimeUnit.SECONDS
                        ).thenApply(result -> {
                            if (result.isSuccess()) {
                                healthMonitor.recordMetric("task." + taskName + ".success", 1);
                                return task.get(); // Execute actual task
                            } else {
                                throw new RuntimeException(result.getError());
                            }
                        });
                        
                        return future.get();
                        
                    } catch (Exception e) {
                        healthMonitor.recordMetric("task." + taskName + ".failure", 1);
                        log.error("Task {} failed: {}", taskName, e.getMessage(), e);
                        throw e;
                    }
                },
                fallback
            );
        });
    }

    /**
     * Execute a resource-intensive task with resource management
     */
    public <T> CompletableFuture<T> executeWithResourceManagement(
            String taskName,
            String resourcePoolName,
            ResourceManager.ResourceFactory<T> resourceFactory,
            java.util.function.Function<T, T> taskLogic,
            Supplier<T> fallback) {
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Acquire resource
                T resource = resourceManager.acquireResource(resourcePoolName, resourceFactory);
                
                try {
                    // Execute task with resource
                    T result = taskLogic.apply(resource);
                    healthMonitor.recordMetric("task." + taskName + ".success", 1);
                    return result;
                    
                } finally {
                    // Always release resource
                    resourceManager.releaseResource(resourcePoolName, resource);
                }
                
            } catch (ResourceManager.ResourceException e) {
                log.error("Resource acquisition failed for task {}: {}", taskName, e.getMessage(), e);
                healthMonitor.recordMetric("task." + taskName + ".resource_failure", 1);
                return fallback.get();
            } catch (Exception e) {
                log.error("Task {} failed: {}", taskName, e.getMessage(), e);
                healthMonitor.recordMetric("task." + taskName + ".failure", 1);
                return fallback.get();
            }
        });
    }

    /**
     * Execute a batch task with progress monitoring
     */
    public <T> CompletableFuture<Void> executeBatchTask(
            String batchName,
            AsyncTaskProcessor.BatchTask batchTask,
            BatchConfig config) {
        
        // Check if system can handle batch processing
        HealthMonitor.SystemHealth health = healthMonitor.getSystemHealth();
        if (health.getMemoryUsage() > 0.8 || health.getThreadCount() > 400) {
            log.warn("System under heavy load, deferring batch task: {}", batchName);
            return CompletableFuture.completedFuture(null);
        }

        return asyncTaskProcessor.submitBatchTask(batchTask).thenRun(() -> {
            healthMonitor.recordMetric("batch." + batchName + ".completed", 1);
            log.info("Batch task completed: {}", batchName);
        }).exceptionally(throwable -> {
            healthMonitor.recordMetric("batch." + batchName + ".failed", 1);
            log.error("Batch task failed: {}", batchName, throwable);
            return null;
        });
    }

    /**
     * Get comprehensive system status
     */
    public SystemStatus getSystemStatus() {
        return SystemStatus.builder()
            .taskStats(asyncTaskProcessor.getStats())
            .health(healthMonitor.getSystemHealth())
            .resourceStats(resourceManager.getSystemResourceStats())
            .circuitBreakerCount(circuitBreakerManager.circuitBreakers.size())
            .build();
    }

    /**
     * Perform emergency shutdown procedures
     */
    public void emergencyShutdown() {
        log.error("EMERGENCY SHUTDOWN INITIATED");
        
        try {
            // Stop accepting new tasks
            healthMonitor.recordMetric("system.emergency_shutdown", 1);
            
            // Shutdown components gracefully
            asyncTaskProcessor.shutdown();
            resourceManager.shutdown();
            healthMonitor.shutdown();
            
            log.info("Emergency shutdown completed");
            
        } catch (Exception e) {
            log.error("Error during emergency shutdown: {}", e.getMessage(), e);
        }
    }

    /**
     * Perform system cleanup
     */
    public void performCleanup() {
        try {
            log.info("Performing system cleanup...");
            
            // Cleanup rate limiters
            rateLimiter.cleanup();
            
            // Cleanup resource pools
            resourceManager.cleanupAllPools();
            
            // Trigger garbage collection
            System.gc();
            
            healthMonitor.recordMetric("system.cleanup", 1);
            log.info("System cleanup completed");
            
        } catch (Exception e) {
            log.error("Error during cleanup: {}", e.getMessage(), e);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class TaskConfig {
        private int rateLimit = 100;
        private int rateWindowSeconds = 60;
        private int timeoutSeconds = 30;
        private int retryAttempts = 3;
        private AsyncTaskProcessor.TaskPriority priority = AsyncTaskProcessor.TaskPriority.NORMAL;
    }

    @lombok.Data
    @lombok.Builder
    public static class BatchConfig {
        private int maxConcurrentTasks = 10;
        private int timeoutMinutes = 30;
        private boolean continueOnError = true;
        private AsyncTaskProcessor.TaskPriority priority = AsyncTaskProcessor.TaskPriority.LOW;
    }

    @lombok.Data
    @lombok.Builder
    public static class SystemStatus {
        private AsyncTaskProcessor.TaskStats taskStats;
        private HealthMonitor.SystemHealth health;
        private ResourceManager.SystemResourceStats resourceStats;
        private int circuitBreakerCount;
    }
}
