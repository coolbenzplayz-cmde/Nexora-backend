package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Comprehensive concurrency configuration for crash prevention and multitasking
 */
@Configuration
@EnableAsync
@Slf4j
public class ConcurrencyConfig implements WebMvcConfigurer {

    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Core pool size based on available processors
        int corePoolSize = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(corePoolSize);
        
        // Maximum pool size for burst handling
        executor.setMaxPoolSize(corePoolSize * 4);
        
        // Queue capacity for task buffering
        executor.setQueueCapacity(1000);
        
        // Thread naming
        executor.setThreadNamePrefix("AsyncTask-");
        
        // Rejection policy to prevent crashes
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Thread configuration
        executor.setThreadPriority(Thread.NORM_PRIORITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // Initialize
        executor.initialize();
        
        log.info("Task executor initialized - Core: {}, Max: {}, Queue: {}", 
                corePoolSize, corePoolSize * 4, 1000);
        
        return executor;
    }

    @Bean(name = "mediaProcessingExecutor")
    public Executor mediaProcessingExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Separate pool for media processing (CPU intensive)
        int corePoolSize = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 2);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("MediaProcessing-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadPriority(Thread.NORM_PRIORITY - 1); // Lower priority for background tasks
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.initialize();
        
        log.info("Media processing executor initialized - Core: {}, Max: {}", 
                corePoolSize, corePoolSize * 2);
        
        return executor;
    }

    @Bean(name = "ioExecutor")
    public Executor ioExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Larger pool for I/O operations
        int corePoolSize = Runtime.getRuntime().availableProcessors() * 2;
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(corePoolSize * 3);
        executor.setQueueCapacity(2000);
        executor.setThreadNamePrefix("IO-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setThreadPriority(Thread.NORM_PRIORITY - 2); // Lower priority for I/O
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        executor.initialize();
        
        log.info("I/O executor initialized - Core: {}, Max: {}", 
                corePoolSize, corePoolSize * 3);
        
        return executor;
    }

    @Bean(name = "scheduledExecutor")
    public Executor scheduledExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Small pool for scheduled tasks
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Scheduled-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.setThreadPriority(Thread.NORM_PRIORITY);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(10);
        
        executor.initialize();
        
        log.info("Scheduled executor initialized - Core: 4, Max: 8");
        
        return executor;
    }

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {
        configurer.setDefaultTaskExecutor(taskExecutor());
        configurer.setTaskExecutor(taskExecutor());
        configurer.setThreadPriority(5);
    }

    @Bean
    public AsyncTaskProcessor asyncTaskProcessor() {
        AsyncTaskProcessor processor = new AsyncTaskProcessor();
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down async task processor...");
            processor.shutdown();
        }));
        
        return processor;
    }

    @Bean
    public CircuitBreakerManager circuitBreakerManager() {
        return new CircuitBreakerManager();
    }

    @Bean
    public ConnectionPoolManager connectionPoolManager() {
        ConnectionPoolManager manager = new ConnectionPoolManager();
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down connection pool manager...");
            manager.shutdown();
        }));
        
        return manager;
    }

    @Bean
    public RateLimiter rateLimiter() {
        return new RateLimiter();
    }

    @Bean
    public HealthMonitor healthMonitor() {
        HealthMonitor monitor = new HealthMonitor();
        monitor.startMonitoring();
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down health monitor...");
            monitor.shutdown();
        }));
        
        return monitor;
    }

    @Bean
    public ResourceManager resourceManager() {
        ResourceManager manager = new ResourceManager();
        
        // Register shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down resource manager...");
            manager.shutdown();
        }));
        
        return manager;
    }

    @Bean
    public ConcurrencyGuard concurrencyGuard() {
        return new ConcurrencyGuard(
            asyncTaskProcessor(),
            circuitBreakerManager(),
            rateLimiter(),
            healthMonitor(),
            resourceManager()
        );
    }
}
