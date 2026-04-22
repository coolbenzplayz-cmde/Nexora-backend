package org.example.nexora.concurrency;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * High-performance async task processor with crash prevention
 */
@Service
@Slf4j
public class AsyncTaskProcessor {

    private final ExecutorService taskExecutor;
    private final ScheduledExecutorService scheduledExecutor;
    private final CompletionService<TaskResult> completionService;
    private final AtomicInteger activeTasks = new AtomicInteger(0);
    private final AtomicInteger completedTasks = new AtomicInteger(0);
    private final AtomicInteger failedTasks = new AtomicInteger(0);

    public AsyncTaskProcessor() {
        this.taskExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors() * 4,
            new ThreadFactory() {
                private final AtomicInteger threadNumber = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "AsyncTask-" + threadNumber.getAndIncrement());
                    t.setDaemon(false);
                    t.setUncaughtExceptionHandler((thread, ex) -> {
                        log.error("Uncaught exception in thread {}: {}", thread.getName(), ex.getMessage(), ex);
                        failedTasks.incrementAndGet();
                    });
                    return t;
                }
            }
        );
        
        this.scheduledExecutor = Executors.newScheduledThreadPool(4);
        this.completionService = new ExecutorCompletionService<>(taskExecutor);
        
        // Monitor task health
        scheduledExecutor.scheduleAtFixedRate(this::monitorHealth, 30, 30, TimeUnit.SECONDS);
    }

    @Async("taskExecutor")
    public CompletableFuture<TaskResult> submitTask(Callable<TaskResult> task, TaskPriority priority) {
        if (activeTasks.get() > 1000) {
            log.warn("Task queue is full, rejecting new task");
            return CompletableFuture.completedFuture(
                TaskResult.failure("Task queue full", "QUEUE_FULL")
            );
        }

        activeTasks.incrementAndGet();
        log.debug("Submitting task, active: {}", activeTasks.get());

        CompletableFuture<TaskResult> future = new CompletableFuture<>();
        
        try {
            Future<TaskResult> taskFuture = taskExecutor.submit(() -> {
                try {
                    TaskResult result = task.call();
                    completedTasks.incrementAndGet();
                    log.debug("Task completed successfully, completed: {}", completedTasks.get());
                    return result;
                } catch (Exception e) {
                    failedTasks.incrementAndGet();
                    log.error("Task failed: {}", e.getMessage(), e);
                    return TaskResult.failure(e.getMessage(), "TASK_FAILED");
                } finally {
                    activeTasks.decrementAndGet();
                }
            });
            
            // Handle completion
            CompletableFuture.runAsync(() -> {
                try {
                    TaskResult result = taskFuture.get(60, TimeUnit.SECONDS);
                    future.complete(result);
                } catch (Exception e) {
                    log.error("Task execution error: {}", e.getMessage(), e);
                    future.complete(TaskResult.failure(e.getMessage(), "EXECUTION_ERROR"));
                }
            });
            
        } catch (RejectedExecutionException e) {
            activeTasks.decrementAndGet();
            future.complete(TaskResult.failure("Task rejected", "REJECTED"));
        }

        return future;
    }

    @Async("taskExecutor")
    public CompletableFuture<Void> submitBatchTask(BatchTask batchTask) {
        return CompletableFuture.runAsync(() -> {
            try {
                batchTask.execute();
                log.info("Batch task completed: {}", batchTask.getTaskId());
            } catch (Exception e) {
                log.error("Batch task failed: {}", e.getMessage(), e);
            }
        }, taskExecutor);
    }

    public CompletableFuture<TaskResult> submitWithTimeout(Callable<TaskResult> task, long timeout, TimeUnit unit) {
        CompletableFuture<TaskResult> future = submitTask(task, TaskPriority.NORMAL);
        
        // Add timeout
        ScheduledFuture<?> timeoutFuture = scheduledExecutor.schedule(() -> {
            if (!future.isDone()) {
                future.cancel(true);
                log.warn("Task timed out after {} {}", timeout, unit);
            }
        }, timeout, unit);

        future.whenComplete((result, throwable) -> {
            timeoutFuture.cancel(false);
        });

        return future;
    }

    private void monitorHealth() {
        int active = activeTasks.get();
        int completed = completedTasks.get();
        int failed = failedTasks.get();
        
        log.info("Task Health - Active: {}, Completed: {}, Failed: {}", active, completed, failed);
        
        // Alert if too many failures
        if (failed > 100 && failed > completed * 0.1) {
            log.error("High failure rate detected - Failed: {}, Completed: {}", failed, completed);
        }
        
        // Alert if queue is backing up
        if (active > 500) {
            log.warn("High task queue - Active: {}", active);
        }
    }

    public TaskStats getStats() {
        return TaskStats.builder()
            .activeTasks(activeTasks.get())
            .completedTasks(completedTasks.get())
            .failedTasks(failedTasks.get())
            .build();
    }

    public void shutdown() {
        log.info("Shutting down task processor...");
        taskExecutor.shutdown();
        scheduledExecutor.shutdown();
        try {
            if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                taskExecutor.shutdownNow();
            }
            if (!scheduledExecutor.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduledExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            taskExecutor.shutdownNow();
            scheduledExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public enum TaskPriority {
        LOW, NORMAL, HIGH, CRITICAL
    }

    @lombok.Data
    @lombok.Builder
    public static class TaskResult {
        private boolean success;
        private String data;
        private String error;
        private String errorCode;
        private long executionTime;

        public static TaskResult success(String data) {
            return TaskResult.builder()
                .success(true)
                .data(data)
                .executionTime(System.currentTimeMillis())
                .build();
        }

        public static TaskResult failure(String error, String errorCode) {
            return TaskResult.builder()
                .success(false)
                .error(error)
                .errorCode(errorCode)
                .executionTime(System.currentTimeMillis())
                .build();
        }
    }

    @lombok.Data
    @lombok.Builder
    public static class TaskStats {
        private int activeTasks;
        private int completedTasks;
        private int failedTasks;
    }

    public interface BatchTask {
        String getTaskId();
        void execute() throws Exception;
    }
}
