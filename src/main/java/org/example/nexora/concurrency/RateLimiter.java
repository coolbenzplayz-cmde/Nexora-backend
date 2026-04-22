package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Distributed rate limiter for crash prevention and load management
 */
@Component
@Slf4j
public class RateLimiter {

    private final ConcurrentHashMap<String, RateLimitBucket> buckets = new ConcurrentHashMap<>();
    
    public boolean allowRequest(String key, int limit, int windowSeconds) {
        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket(limit, windowSeconds));
        return bucket.allowRequest();
    }

    public boolean allowRequest(String key, int limit, int windowSeconds, int burstCapacity) {
        RateLimitBucket bucket = buckets.computeIfAbsent(key, k -> new RateLimitBucket(limit, windowSeconds, burstCapacity));
        return bucket.allowRequest();
    }

    public RateLimitStatus getStatus(String key) {
        RateLimitBucket bucket = buckets.get(key);
        return bucket != null ? bucket.getStatus() : RateLimitStatus.allowed();
    }

    public void resetBucket(String key) {
        RateLimitBucket bucket = buckets.get(key);
        if (bucket != null) {
            bucket.reset();
        }
    }

    public void cleanup() {
        long currentTime = System.currentTimeMillis();
        buckets.entrySet().removeIf(entry -> {
            RateLimitBucket bucket = entry.getValue();
            return currentTime - bucket.getLastResetTime() > bucket.getWindowSizeMs() * 10;
        });
    }

    private static class RateLimitBucket {
        private final int limit;
        private final int windowSeconds;
        private final int burstCapacity;
        private final AtomicInteger currentTokens = new AtomicInteger();
        private final AtomicLong lastResetTime = new AtomicLong(System.currentTimeMillis());
        private final AtomicInteger rejectedRequests = new AtomicInteger(0);
        private final AtomicInteger totalRequests = new AtomicInteger(0);

        public RateLimitBucket(int limit, int windowSeconds) {
            this(limit, windowSeconds, limit);
        }

        public RateLimitBucket(int limit, int windowSeconds, int burstCapacity) {
            this.limit = limit;
            this.windowSeconds = windowSeconds;
            this.burstCapacity = burstCapacity;
            this.currentTokens.set(limit);
        }

        public synchronized boolean allowRequest() {
            long currentTime = System.currentTimeMillis();
            long windowStart = currentTime - (windowSeconds * 1000L);
            
            // Reset if window has passed
            if (currentTime - lastResetTime.get() >= windowSeconds * 1000L) {
                currentTokens.set(limit);
                lastResetTime.set(currentTime);
            }

            totalRequests.incrementAndGet();

            if (currentTokens.get() > 0) {
                currentTokens.decrementAndGet();
                return true;
            } else {
                rejectedRequests.incrementAndGet();
                return false;
            }
        }

        public RateLimitStatus getStatus() {
            long currentTime = System.currentTimeMillis();
            long timeUntilReset = (windowSeconds * 1000L) - (currentTime - lastResetTime.get());
            
            return RateLimitStatus.builder()
                .allowed(currentTokens.get() > 0)
                .remainingTokens(currentTokens.get())
                .limit(limit)
                .windowSeconds(windowSeconds)
                .timeUntilReset(Math.max(0, timeUntilReset))
                .rejectedRequests(rejectedRequests.get())
                .totalRequests(totalRequests.get())
                .build();
        }

        public void reset() {
            currentTokens.set(limit);
            lastResetTime.set(System.currentTimeMillis());
            rejectedRequests.set(0);
            totalRequests.set(0);
        }

        public int getWindowSizeMs() {
            return windowSeconds * 1000;
        }

        public long getLastResetTime() {
            return lastResetTime.get();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class RateLimitStatus {
        private boolean allowed;
        private int remainingTokens;
        private int limit;
        private int windowSeconds;
        private long timeUntilReset;
        private int rejectedRequests;
        private int totalRequests;

        public static RateLimitStatus allowed() {
            return RateLimitStatus.builder()
                .allowed(true)
                .remainingTokens(Integer.MAX_VALUE)
                .limit(Integer.MAX_VALUE)
                .windowSeconds(0)
                .timeUntilReset(0)
                .rejectedRequests(0)
                .totalRequests(0)
                .build();
        }
    }

    // Sliding window rate limiter for more precise control
    public static class SlidingWindowRateLimiter {
        private final ConcurrentHashMap<String, SlidingWindow> windows = new ConcurrentHashMap<>();

        public boolean allowRequest(String key, int limit, int windowSeconds) {
            SlidingWindow window = windows.computeIfAbsent(key, k -> new SlidingWindow(limit, windowSeconds));
            return window.allowRequest();
        }

        private static class SlidingWindow {
            private final int limit;
            private final int windowSeconds;
            private final ConcurrentHashMap<Long, AtomicInteger> requests = new ConcurrentHashMap<>();
            private final AtomicLong lastCleanup = new AtomicLong(System.currentTimeMillis());

            public SlidingWindow(int limit, int windowSeconds) {
                this.limit = limit;
                this.windowSeconds = windowSeconds;
            }

            public synchronized boolean allowRequest() {
                long currentTime = System.currentTimeMillis();
                long windowStart = currentTime - (windowSeconds * 1000L);
                
                // Clean old entries
                cleanup(windowStart);
                
                // Count requests in current window
                int requestCount = requests.values().stream()
                    .mapToInt(AtomicInteger::get)
                    .sum();
                
                if (requestCount < limit) {
                    long secondKey = currentTime / 1000;
                    requests.computeIfAbsent(secondKey, k -> new AtomicInteger(0)).incrementAndGet();
                    return true;
                }
                
                return false;
            }

            private void cleanup(long windowStart) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastCleanup.get() < 60000) { // Cleanup every minute
                    return;
                }
                
                long secondKey = windowStart / 1000;
                requests.entrySet().removeIf(entry -> entry.getKey() < secondKey);
                lastCleanup.set(currentTime);
            }
        }
    }

    // Token bucket rate limiter for burst handling
    public static class TokenBucketRateLimiter {
        private final ConcurrentHashMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

        public boolean allowRequest(String key, int refillRate, int burstCapacity) {
            TokenBucket bucket = buckets.computeIfAbsent(key, k -> new TokenBucket(refillRate, burstCapacity));
            return bucket.consumeToken();
        }

        private static class TokenBucket {
            private final int refillRate;
            private final int burstCapacity;
            private volatile int tokens;
            private volatile long lastRefillTime;

            public TokenBucket(int refillRate, int burstCapacity) {
                this.refillRate = refillRate;
                this.burstCapacity = burstCapacity;
                this.tokens = burstCapacity;
                this.lastRefillTime = System.currentTimeMillis();
            }

            public synchronized boolean consumeToken() {
                refill();
                
                if (tokens > 0) {
                    tokens--;
                    return true;
                }
                
                return false;
            }

            private void refill() {
                long currentTime = System.currentTimeMillis();
                long timeSinceLastRefill = currentTime - lastRefillTime;
                
                if (timeSinceLastRefill >= 1000) { // Refill every second
                    int tokensToAdd = (int) (timeSinceLastRefill / 1000) * refillRate;
                    tokens = Math.min(burstCapacity, tokens + tokensToAdd);
                    lastRefillTime = currentTime;
                }
            }
        }
    }
}
