package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * System health monitor for crash prevention and performance optimization
 */
@Component
@Slf4j
public class HealthMonitor {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);
    private final ConcurrentHashMap<String, HealthMetric> metrics = new ConcurrentHashMap<>();
    private final AtomicInteger alertCount = new AtomicInteger(0);
    private final AtomicLong lastAlertTime = new AtomicLong(0);
    
    private final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
    private final MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
    
    // Health thresholds
    private static final double CPU_THRESHOLD = 0.85;
    private static final double MEMORY_THRESHOLD = 0.85;
    private static final int THREAD_THRESHOLD = 500;
    private static final int ACTIVE_TASKS_THRESHOLD = 1000;
    private static final int FAILED_REQUESTS_THRESHOLD = 100;

    public void startMonitoring() {
        // Monitor system resources every 30 seconds
        scheduler.scheduleAtFixedRate(this::monitorSystemHealth, 0, 30, TimeUnit.SECONDS);
        
        // Monitor application metrics every 60 seconds
        scheduler.scheduleAtFixedRate(this::monitorApplicationHealth, 10, 60, TimeUnit.SECONDS);
        
        // Cleanup old metrics every 5 minutes
        scheduler.scheduleAtFixedRate(this::cleanupMetrics, 60, 300, TimeUnit.SECONDS);
        
        log.info("Health monitoring started");
    }

    private void monitorSystemHealth() {
        try {
            // Memory usage
            long usedMemory = memoryBean.getHeapMemoryUsage().getUsed();
            long maxMemory = memoryBean.getHeapMemoryUsage().getMax();
            double memoryUsage = (double) usedMemory / maxMemory;
            
            recordMetric("memory.used", usedMemory);
            recordMetric("memory.max", maxMemory);
            recordMetric("memory.usage_percent", memoryUsage * 100);
            
            if (memoryUsage > MEMORY_THRESHOLD) {
                alert("High memory usage: " + String.format("%.2f%%", memoryUsage * 100));
            }
            
            // Thread count
            int threadCount = threadBean.getThreadCount();
            recordMetric("threads.count", threadCount);
            
            if (threadCount > THREAD_THRESHOLD) {
                alert("High thread count: " + threadCount);
            }
            
            // CPU usage (approximate)
            double cpuUsage = estimateCpuUsage();
            recordMetric("cpu.usage_percent", cpuUsage * 100);
            
            if (cpuUsage > CPU_THRESHOLD) {
                alert("High CPU usage: " + String.format("%.2f%%", cpuUsage * 100));
            }
            
        } catch (Exception e) {
            log.error("Error monitoring system health: {}", e.getMessage(), e);
        }
    }

    private void monitorApplicationHealth() {
        try {
            // Monitor task queues
            AsyncTaskProcessor.TaskStats taskStats = getTaskStats();
            if (taskStats != null) {
                recordMetric("tasks.active", taskStats.getActiveTasks());
                recordMetric("tasks.completed", taskStats.getCompletedTasks());
                recordMetric("tasks.failed", taskStats.getFailedTasks());
                
                if (taskStats.getActiveTasks() > ACTIVE_TASKS_THRESHOLD) {
                    alert("High active task count: " + taskStats.getActiveTasks());
                }
                
                double failureRate = taskStats.getFailedTasks() > 0 ? 
                    (double) taskStats.getFailedTasks() / (taskStats.getCompletedTasks() + taskStats.getFailedTasks()) : 0;
                
                if (failureRate > 0.1 && taskStats.getFailedTasks() > FAILED_REQUESTS_THRESHOLD) {
                    alert("High task failure rate: " + String.format("%.2f%%", failureRate * 100));
                }
            }
            
            // Monitor connection pools
            // This would be implemented based on actual connection pool stats
            
            // Monitor circuit breakers
            // This would be implemented based on actual circuit breaker stats
            
        } catch (Exception e) {
            log.error("Error monitoring application health: {}", e.getMessage(), e);
        }
    }

    private void cleanupMetrics() {
        long cutoffTime = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 hours ago
        metrics.entrySet().removeIf(entry -> entry.getValue().getLastUpdated() < cutoffTime);
    }

    public void recordMetric(String name, double value) {
        HealthMetric metric = metrics.computeIfAbsent(name, HealthMetric::new);
        metric.addValue(value);
    }

    public void recordMetric(String name, long value) {
        recordMetric(name, (double) value);
    }

    public HealthMetric getMetric(String name) {
        return metrics.get(name);
    }

    private void alert(String message) {
        long currentTime = System.currentTimeMillis();
        
        // Rate limit alerts to avoid spam
        if (currentTime - lastAlertTime.get() > 60000) { // 1 minute between alerts
            log.warn("HEALTH ALERT: {}", message);
            alertCount.incrementAndGet();
            lastAlertTime.set(currentTime);
            
            // Trigger emergency actions if needed
            handleHealthAlert(message);
        }
    }

    private void handleHealthAlert(String alertMessage) {
        // Implement emergency actions based on alert type
        if (alertMessage.contains("memory")) {
            // Trigger garbage collection
            System.gc();
            log.info("Triggered garbage collection due to memory pressure");
        }
        
        if (alertMessage.contains("CPU")) {
            // Could implement CPU throttling or task prioritization
            log.info("CPU pressure detected, consider task prioritization");
        }
        
        if (alertMessage.contains("thread")) {
            // Could implement thread pool cleanup
            log.info("Thread pressure detected, consider thread pool cleanup");
        }
    }

    private double estimateCpuUsage() {
        // This is a simplified estimation
        // In a real implementation, you'd use proper CPU monitoring
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        if (osBean instanceof com.sun.management.OperatingSystemMXBean) {
            return ((com.sun.management.OperatingSystemMXBean) osBean).getProcessCpuLoad();
        }
        return 0.0;
    }

    private AsyncTaskProcessor.TaskStats getTaskStats() {
        // This would be injected or retrieved from the actual task processor
        return null;
    }

    public SystemHealth getSystemHealth() {
        return SystemHealth.builder()
            .memoryUsage(getMemoryUsage())
            .threadCount(threadBean.getThreadCount())
            .cpuUsage(estimateCpuUsage())
            .alertCount(alertCount.get())
            .metricCount(metrics.size())
            .build();
    }

    private double getMemoryUsage() {
        long used = memoryBean.getHeapMemoryUsage().getUsed();
        long max = memoryBean.getHeapMemoryUsage().getMax();
        return (double) used / max;
    }

    public void shutdown() {
        log.info("Shutting down health monitor...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class SystemHealth {
        private double memoryUsage;
        private int threadCount;
        private double cpuUsage;
        private int alertCount;
        private int metricCount;
        private String status;
    }

    private static class HealthMetric {
        private final String name;
        private volatile double currentValue;
        private volatile double minValue = Double.MAX_VALUE;
        private volatile double maxValue = Double.MIN_VALUE;
        private volatile long lastUpdated = System.currentTimeMillis();
        private final AtomicInteger sampleCount = new AtomicInteger(0);
        private volatile double sum = 0.0;

        public HealthMetric(String name) {
            this.name = name;
        }

        public void addValue(double value) {
            currentValue = value;
            minValue = Math.min(minValue, value);
            maxValue = Math.max(maxValue, value);
            sum += value;
            sampleCount.incrementAndGet();
            lastUpdated = System.currentTimeMillis();
        }

        public double getAverage() {
            int count = sampleCount.get();
            return count > 0 ? sum / count : 0.0;
        }

        public String getName() { return name; }
        public double getCurrentValue() { return currentValue; }
        public double getMinValue() { return minValue; }
        public double getMaxValue() { return maxValue; }
        public long getLastUpdated() { return lastUpdated; }
        public int getSampleCount() { return sampleCount.get(); }
    }
}
