package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance connection pool manager for crash prevention
 */
@Component
@Slf4j
public class ConnectionPoolManager {

    private final ConcurrentHashMap<String, ConnectionPool> pools = new ConcurrentHashMap<>();
    private final ScheduledExecutorService monitorExecutor = java.util.concurrent.Executors.newScheduledThreadPool(2);

    public ConnectionPool getPool(String poolName, DataSource dataSource) {
        return pools.computeIfAbsent(poolName, name -> new ConnectionPool(name, dataSource));
    }

    public <T> T executeWithConnection(String poolName, DataSource dataSource, ConnectionCallback<T> callback) {
        ConnectionPool pool = getPool(poolName, dataSource);
        return pool.execute(callback);
    }

    public class ConnectionPool {
        private final String poolName;
        private final DataSource dataSource;
        private final BlockingQueue<PooledConnection> availableConnections;
        private final AtomicInteger totalConnections = new AtomicInteger(0);
        private final AtomicInteger activeConnections = new AtomicInteger(0);
        private final AtomicInteger borrowedConnections = new AtomicInteger(0);
        
        private final int maxPoolSize = 50;
        private final int minPoolSize = 5;
        private final long maxIdleTime = 300000; // 5 minutes
        private final long connectionTimeout = 30000; // 30 seconds

        public ConnectionPool(String poolName, DataSource dataSource) {
            this.poolName = poolName;
            this.dataSource = dataSource;
            this.availableConnections = new ArrayBlockingQueue<>(maxPoolSize);
            
            // Initialize minimum connections
            for (int i = 0; i < minPoolSize; i++) {
                createConnection();
            }
            
            // Monitor pool health
            monitorExecutor.scheduleAtFixedRate(this::monitorPool, 60, 60, TimeUnit.SECONDS);
        }

        public <T> T execute(ConnectionCallback<T> callback) {
            PooledConnection connection = null;
            try {
                connection = borrowConnection();
                T result = callback.execute(connection.getConnection());
                returnConnection(connection);
                return result;
            } catch (Exception e) {
                if (connection != null) {
                    invalidateConnection(connection);
                }
                log.error("Error executing connection callback for pool {}: {}", poolName, e.getMessage(), e);
                throw new RuntimeException("Connection execution failed", e);
            }
        }

        private PooledConnection borrowConnection() throws SQLException {
            PooledConnection connection = availableConnections.poll();
            
            if (connection == null) {
                // Try to create new connection if under limit
                if (totalConnections.get() < maxPoolSize) {
                    connection = createConnection();
                } else {
                    // Wait for available connection
                    try {
                        connection = availableConnections.poll(connectionTimeout, TimeUnit.MILLISECONDS);
                        if (connection == null) {
                            throw new SQLException("Connection timeout for pool: " + poolName);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new SQLException("Interrupted while waiting for connection", e);
                    }
                }
            }
            
            // Validate connection
            if (!connection.isValid()) {
                invalidateConnection(connection);
                return borrowConnection(); // Retry with new connection
            }
            
            borrowedConnections.incrementAndGet();
            log.debug("Borrowed connection from pool {}, active: {}", poolName, borrowedConnections.get());
            return connection;
        }

        private void returnConnection(PooledConnection connection) {
            if (connection != null && connection.isValid()) {
                if (availableConnections.offer(connection)) {
                    borrowedConnections.decrementAndGet();
                    log.debug("Returned connection to pool {}, active: {}", poolName, borrowedConnections.get());
                } else {
                    // Pool full, close connection
                    invalidateConnection(connection);
                }
            } else {
                invalidateConnection(connection);
            }
        }

        private void invalidateConnection(PooledConnection connection) {
            if (connection != null) {
                try {
                    connection.close();
                    totalConnections.decrementAndGet();
                    borrowedConnections.decrementAndGet();
                    log.debug("Invalidated connection in pool {}, total: {}", poolName, totalConnections.get());
                } catch (SQLException e) {
                    log.error("Error closing connection: {}", e.getMessage(), e);
                }
            }
        }

        private PooledConnection createConnection() {
            try {
                Connection connection = dataSource.getConnection();
                PooledConnection pooledConnection = new PooledConnection(connection, System.currentTimeMillis());
                if (availableConnections.offer(pooledConnection)) {
                    totalConnections.incrementAndGet();
                    log.debug("Created new connection for pool {}, total: {}", poolName, totalConnections.get());
                    return pooledConnection;
                } else {
                    connection.close();
                    return null;
                }
            } catch (SQLException e) {
                log.error("Error creating connection for pool {}: {}", poolName, e.getMessage(), e);
                return null;
            }
        }

        private void monitorPool() {
            int total = totalConnections.get();
            int active = borrowedConnections.get();
            int available = availableConnections.size();
            
            log.info("Pool {} stats - Total: {}, Active: {}, Available: {}", poolName, total, active, available);
            
            // Remove idle connections
            long currentTime = System.currentTimeMillis();
            availableConnections.removeIf(conn -> {
                if (currentTime - conn.getCreatedTime() > maxIdleTime && totalConnections.get() > minPoolSize) {
                    try {
                        conn.close();
                        totalConnections.decrementAndGet();
                        log.debug("Removed idle connection from pool {}", poolName);
                        return true;
                    } catch (SQLException e) {
                        log.error("Error removing idle connection: {}", e.getMessage(), e);
                    }
                }
                return false;
            });
            
            // Ensure minimum connections
            while (totalConnections.get() < minPoolSize) {
                createConnection();
            }
        }

        public PoolStats getStats() {
            return PoolStats.builder()
                .poolName(poolName)
                .totalConnections(totalConnections.get())
                .activeConnections(borrowedConnections.get())
                .availableConnections(availableConnections.size())
                .maxPoolSize(maxPoolSize)
                .build();
        }

        public void shutdown() {
            log.info("Shutting down connection pool {}", poolName);
            availableConnections.forEach(conn -> {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error("Error closing connection during shutdown: {}", e.getMessage(), e);
                }
            });
            availableConnections.clear();
            totalConnections.set(0);
            borrowedConnections.set(0);
        }
    }

    private static class PooledConnection {
        private final Connection connection;
        private final long createdTime;

        public PooledConnection(Connection connection, long createdTime) {
            this.connection = connection;
            this.createdTime = createdTime;
        }

        public Connection getConnection() {
            return connection;
        }

        public long getCreatedTime() {
            return createdTime;
        }

        public boolean isValid() {
            try {
                return connection != null && !connection.isClosed() && connection.isValid(1);
            } catch (SQLException e) {
                return false;
            }
        }

        public void close() throws SQLException {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        }
    }

    @FunctionalInterface
    public interface ConnectionCallback<T> {
        T execute(Connection connection) throws SQLException;
    }

    @lombok.Data
    @lombok.Builder
    public static class PoolStats {
        private String poolName;
        private int totalConnections;
        private int activeConnections;
        private int availableConnections;
        private int maxPoolSize;
    }

    public void shutdown() {
        log.info("Shutting down connection pool manager...");
        monitorExecutor.shutdown();
        pools.values().forEach(ConnectionPool::shutdown);
        pools.clear();
    }
}
