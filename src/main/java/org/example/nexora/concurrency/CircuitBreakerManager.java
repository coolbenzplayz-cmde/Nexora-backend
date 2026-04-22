package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Circuit Breaker pattern implementation for crash prevention
 */
@Component
@Slf4j
public class CircuitBreakerManager {

    private final ConcurrentHashMap<String, CircuitBreaker> circuitBreakers = new ConcurrentHashMap<>();
    
    private static final int FAILURE_THRESHOLD = 5;
    private static final int TIMEOUT_THRESHOLD = 30000; // 30 seconds
    private static final int RECOVERY_TIMEOUT = 60000; // 1 minute

    public CircuitBreaker getCircuitBreaker(String serviceName) {
        return circuitBreakers.computeIfAbsent(serviceName, name -> new CircuitBreaker(name));
    }

    public <T> T executeWithCircuitBreaker(String serviceName, Supplier<T> supplier, Supplier<T> fallback) {
        CircuitBreaker circuitBreaker = getCircuitBreaker(serviceName);
        return circuitBreaker.execute(supplier, fallback);
    }

    public class CircuitBreaker {
        private final String serviceName;
        private final AtomicInteger failureCount = new AtomicInteger(0);
        private final AtomicLong lastFailureTime = new AtomicLong(0);
        private volatile boolean isOpen = false;
        private volatile boolean isHalfOpen = false;

        public CircuitBreaker(String serviceName) {
            this.serviceName = serviceName;
        }

        public <T> T execute(Supplier<T> supplier, Supplier<T> fallback) {
            long currentTime = System.currentTimeMillis();

            if (isOpen) {
                if (currentTime - lastFailureTime.get() > RECOVERY_TIMEOUT) {
                    isHalfOpen = true;
                    isOpen = false;
                    log.info("Circuit breaker for {} entering half-open state", serviceName);
                } else {
                    log.warn("Circuit breaker open for {}, using fallback", serviceName);
                    return fallback.get();
                }
            }

            try {
                T result = supplier.get();
                
                if (isHalfOpen) {
                    isHalfOpen = false;
                    failureCount.set(0);
                    log.info("Circuit breaker for {} closed after successful call", serviceName);
                }
                
                return result;
                
            } catch (Exception e) {
                handleFailure(currentTime);
                log.error("Circuit breaker caught exception for {}: {}", serviceName, e.getMessage());
                return fallback.get();
            }
        }

        private void handleFailure(long currentTime) {
            int failures = failureCount.incrementAndGet();
            lastFailureTime.set(currentTime);

            if (failures >= FAILURE_THRESHOLD) {
                isOpen = true;
                isHalfOpen = false;
                log.error("Circuit breaker opened for {} after {} failures", serviceName, failures);
            }
        }

        public CircuitBreakerState getState() {
            if (isOpen) return CircuitBreakerState.OPEN;
            if (isHalfOpen) return CircuitBreakerState.HALF_OPEN;
            return CircuitBreakerState.CLOSED;
        }

        public void reset() {
            failureCount.set(0);
            lastFailureTime.set(0);
            isOpen = false;
            isHalfOpen = false;
            log.info("Circuit breaker for {} manually reset", serviceName);
        }

        public int getFailureCount() {
            return failureCount.get();
        }

        public long getLastFailureTime() {
            return lastFailureTime.get();
        }
    }

    public enum CircuitBreakerState {
        CLOSED, OPEN, HALF_OPEN
    }

    @FunctionalInterface
    public interface Supplier<T> {
        T get() throws Exception;
    }
}
