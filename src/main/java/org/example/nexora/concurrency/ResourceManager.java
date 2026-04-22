package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central resource manager for preventing resource exhaustion and crashes
 */
@Component
@Slf4j
public class ResourceManager {

    private final ConcurrentHashMap<String, ResourcePool> resourcePools = new ConcurrentHashMap<>();
    private final AtomicInteger totalAllocatedMemory = new AtomicInteger(0);
    private final AtomicInteger totalOpenFiles = new AtomicInteger(0);
    private final AtomicInteger totalActiveConnections = new AtomicInteger(0);
    
    // Resource limits
    private static final long MAX_MEMORY_PER_POOL = 100 * 1024 * 1024; // 100MB per pool
    private static final int MAX_FILES_PER_POOL = 1000;
    private static final int MAX_CONNECTIONS_PER_POOL = 100;
    private static final long MAX_TOTAL_MEMORY = 1024 * 1024 * 1024; // 1GB total

    public <T> T acquireResource(String poolName, ResourceFactory<T> factory) throws ResourceException {
        ResourcePool<T> pool = (ResourcePool<T>) resourcePools.computeIfAbsent(poolName, k -> new ResourcePool<>(k, MAX_MEMORY_PER_POOL, MAX_FILES_PER_POOL, MAX_CONNECTIONS_PER_POOL));
        return pool.acquire(factory);
    }

    public void releaseResource(String poolName, Object resource) {
        ResourcePool pool = resourcePools.get(poolName);
        if (pool != null) {
            pool.release(resource);
        }
    }

    public ResourceStats getResourceStats(String poolName) {
        ResourcePool pool = resourcePools.get(poolName);
        return pool != null ? pool.getStats() : ResourceStats.empty();
    }

    public SystemResourceStats getSystemResourceStats() {
        return SystemResourceStats.builder()
            .totalAllocatedMemory(totalAllocatedMemory.get())
            .totalOpenFiles(totalOpenFiles.get())
            .totalActiveConnections(totalActiveConnections.get())
            .activePools(resourcePools.size())
            .build();
    }

    private class ResourcePool<T> {
        private final String poolName;
        private final long maxMemory;
        private final int maxFiles;
        private final int maxConnections;
        private final ConcurrentHashMap<T, ResourceInfo<T>> activeResources = new ConcurrentHashMap<>();
        private final AtomicInteger allocatedMemory = new AtomicInteger(0);
        private final AtomicInteger openFiles = new AtomicInteger(0);
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicLong lastCleanup = new AtomicLong(System.currentTimeMillis());

        public ResourcePool(String poolName, long maxMemory, int maxFiles, int maxConnections) {
            this.poolName = poolName;
            this.maxMemory = maxMemory;
            this.maxFiles = maxFiles;
            this.maxConnections = maxConnections;
        }

        public T acquire(ResourceFactory<T> factory) throws ResourceException {
            // Check resource limits
            if (allocatedMemory.get() > maxMemory) {
                throw new ResourceException("Memory limit exceeded for pool: " + poolName);
            }
            
            if (openFiles.get() > maxFiles) {
                throw new ResourceException("File limit exceeded for pool: " + poolName);
            }
            
            if (activeConnections.get() > maxConnections) {
                throw new ResourceException("Connection limit exceeded for pool: " + poolName);
            }

            try {
                T resource = factory.create();
                ResourceInfo<T> info = new ResourceInfo<>(resource, System.currentTimeMillis());
                activeResources.put(resource, info);
                
                // Update counters
                if (resource instanceof java.io.Closeable) {
                    openFiles.incrementAndGet();
                    totalOpenFiles.incrementAndGet();
                }
                
                if (resource instanceof java.sql.Connection) {
                    activeConnections.incrementAndGet();
                    totalActiveConnections.incrementAndGet();
                }
                
                // Estimate memory usage (simplified)
                int estimatedMemory = estimateResourceSize(resource);
                allocatedMemory.addAndGet(estimatedMemory);
                totalAllocatedMemory.addAndGet(estimatedMemory);
                
                log.debug("Acquired resource for pool {}, active: {}", poolName, activeResources.size());
                return resource;
                
            } catch (Exception e) {
                throw new ResourceException("Failed to create resource: " + e.getMessage(), e);
            }
        }

        public void release(T resource) {
            ResourceInfo<T> info = activeResources.remove(resource);
            if (info != null) {
                try {
                    if (resource instanceof java.io.Closeable) {
                        ((java.io.Closeable) resource).close();
                        openFiles.decrementAndGet();
                        totalOpenFiles.decrementAndGet();
                    }
                    
                    if (resource instanceof java.sql.Connection) {
                        ((java.sql.Connection) resource).close();
                        activeConnections.decrementAndGet();
                        totalActiveConnections.decrementAndGet();
                    }
                    
                    int estimatedMemory = estimateResourceSize(resource);
                    allocatedMemory.addAndGet(-estimatedMemory);
                    totalAllocatedMemory.addAndGet(-estimatedMemory);
                    
                    log.debug("Released resource for pool {}, active: {}", poolName, activeResources.size());
                    
                } catch (Exception e) {
                    log.error("Error releasing resource: {}", e.getMessage(), e);
                }
            }
        }

        private int estimateResourceSize(T resource) {
            // Simplified estimation - in real implementation, you'd use more sophisticated methods
            if (resource instanceof byte[]) {
                return ((byte[]) resource).length;
            } else if (resource instanceof String) {
                return ((String) resource).length() * 2; // UTF-16
            } else if (resource instanceof java.sql.Connection) {
                return 1024; // Estimate 1KB per connection
            } else {
                return 512; // Default estimate
            }
        }

        public void cleanup() {
            long currentTime = System.currentTimeMillis();
            if (currentTime - lastCleanup.get() < 60000) { // Cleanup every minute
                return;
            }
            
            // Remove resources that have been inactive for more than 5 minutes
            long cutoffTime = currentTime - (5 * 60 * 1000);
            activeResources.entrySet().removeIf(entry -> {
                ResourceInfo<T> info = entry.getValue();
                if (info.getCreatedTime() < cutoffTime) {
                    release(entry.getKey());
                    return true;
                }
                return false;
            });
            
            lastCleanup.set(currentTime);
        }

        public ResourceStats getStats() {
            return ResourceStats.builder()
                .poolName(poolName)
                .activeResources(activeResources.size())
                .allocatedMemory(allocatedMemory.get())
                .openFiles(openFiles.get())
                .activeConnections(activeConnections.get())
                .maxMemory(maxMemory)
                .maxFiles(maxFiles)
                .maxConnections(maxConnections)
                .build();
        }
    }

    private static class ResourceInfo<T> {
        private final T resource;
        private final long createdTime;

        public ResourceInfo(T resource, long createdTime) {
            this.resource = resource;
            this.createdTime = createdTime;
        }

        public T getResource() { return resource; }
        public long getCreatedTime() { return createdTime; }
    }

    @FunctionalInterface
    public interface ResourceFactory<T> {
        T create() throws Exception;
    }

    public static class ResourceException extends Exception {
        public ResourceException(String message) {
            super(message);
        }
        
        public ResourceException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class ResourceStats {
        private String poolName;
        private int activeResources;
        private int allocatedMemory;
        private int openFiles;
        private int activeConnections;
        private long maxMemory;
        private int maxFiles;
        private int maxConnections;

        public static ResourceStats empty() {
            return ResourceStats.builder().build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class SystemResourceStats {
        private int totalAllocatedMemory;
        private int totalOpenFiles;
        private int totalActiveConnections;
        private int activePools;
    }

    public void cleanupAllPools() {
        resourcePools.values().forEach(ResourcePool::cleanup);
    }

    public void shutdown() {
        log.info("Shutting down resource manager...");
        
        // Release all resources
        resourcePools.values().forEach(pool -> {
            pool.activeResources.keySet().forEach(pool::release);
        });
        
        resourcePools.clear();
        totalAllocatedMemory.set(0);
        totalOpenFiles.set(0);
        totalActiveConnections.set(0);
    }
}
