package com.megacreative.coding.executors;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Map;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;

/**
 * 🎆 Reference System-Style Advanced Execution Engine
 * 
 * Provides sophisticated script execution modes for optimal performance:
 * - Synchronous execution for real-time operations
 * - Asynchronous execution for heavy computations
 * - Batch execution for multiple scripts
 * - Prioritized execution queues
 * - Performance monitoring and throttling
 * - Execution context isolation
 * 
 * Features:
 * - Thread-safe execution management
 * - Automatic performance optimization
 * - Execution time tracking and limits
 * - Priority-based scheduling
 * - Resource usage monitoring
 */
public class AdvancedExecutionEngine {
    
    public enum ExecutionMode {
        SYNCHRONOUS,        
        ASYNCHRONOUS,      
        DELAYED,           
        BATCH,             
        PRIORITIZED        
    }
    
    public enum Priority {
        CRITICAL(0),    
        HIGH(1),        
        NORMAL(2),      
        LOW(3),         
        IDLE(4);        
        
        private final int level;
        
        Priority(int level) {
            this.level = level;
        }
        
        public int getLevel() {
            return level;
        }
    }
    
    private final MegaCreative plugin;
    private final ThreadPoolExecutor asyncExecutor;
    private final Map<UUID, ExecutionSession> activeSessions;
    private final Map<Priority, List<ExecutionTask>> priorityQueues;
    private final ExecutionMonitor monitor;
    
    
    private static final int MAX_ASYNC_THREADS = 4;
    private static final int MAX_EXECUTION_TIME_MS = 5000; 
    private static final int MAX_INSTRUCTIONS_PER_TICK = 1000;
    private static final int BATCH_SIZE = 10;
    
    
    private long totalExecutions = 0;
    private long successfulExecutions = 0;
    private long failedExecutions = 0;
    private long averageExecutionTime = 0;
    
    public AdvancedExecutionEngine(MegaCreative plugin) {
        this.plugin = plugin;
        this.asyncExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(MAX_ASYNC_THREADS);
        this.activeSessions = new ConcurrentHashMap<>();
        this.priorityQueues = new ConcurrentHashMap<>();
        this.monitor = new ExecutionMonitor();
        
        
        for (Priority priority : Priority.values()) {
            priorityQueues.put(priority, new ArrayList<>());
        }
        
        
        startBackgroundProcessor();
        
        plugin.getLogger().fine("🎆 Advanced Execution Engine initialized with " + MAX_ASYNC_THREADS + " async threads");
    }
    
    /**
     * Execute a script with specified mode and priority
     */
    public CompletableFuture<ExecutionResult> executeScript(CodeScript script, Player player, 
                                                           ExecutionMode mode, Priority priority, String trigger) {
        UUID sessionId = UUID.randomUUID();
        ExecutionSession session = new ExecutionSession(sessionId, script, player, mode, priority, trigger);
        activeSessions.put(sessionId, session);
        
        totalExecutions++;
        long startTime = System.currentTimeMillis();
        
        CompletableFuture<ExecutionResult> future = new CompletableFuture<>();
        
        switch (mode) {
            case SYNCHRONOUS:
                return executeSynchronous(session, future, startTime);
            
            case ASYNCHRONOUS:
                return executeAsynchronous(session, future, startTime);
            
            case DELAYED:
                return executeDelayed(session, future, startTime, 20L); 
            
            case BATCH:
                return executeBatch(session, future, startTime);
            
            case PRIORITIZED:
                return executePrioritized(session, future, startTime);
            
            default:
                future.complete(ExecutionResult.error("Unknown execution mode: " + mode));
                return future;
        }
    }
    
    /**
     * Execute script synchronously on main thread
     */
    private CompletableFuture<ExecutionResult> executeSynchronous(ExecutionSession session, 
                                                                 CompletableFuture<ExecutionResult> future, long startTime) {
        try {
            
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        ExecutionResult result = executeScriptInternal(session);
                        recordExecution(startTime, result.isSuccess());
                        future.complete(result);
                    } catch (Exception e) {
                        recordExecution(startTime, false);
                        future.complete(ExecutionResult.error("Sync execution failed: " + e.getMessage()));
                    } finally {
                        activeSessions.remove(session.getId());
                    }
                }
            }.runTask(plugin);
            
        } catch (Exception e) {
            recordExecution(startTime, false);
            future.complete(ExecutionResult.error("Failed to schedule sync execution: " + e.getMessage()));
        }
        
        return future;
    }
    
    /**
     * Execute script asynchronously on thread pool
     */
    private CompletableFuture<ExecutionResult> executeAsynchronous(ExecutionSession session, 
                                                                  CompletableFuture<ExecutionResult> future, long startTime) {
        try {
            
            asyncExecutor.submit(() -> {
                try {
                    ExecutionResult result = executeScriptInternal(session);
                    recordExecution(startTime, result.isSuccess());
                    future.complete(result);
                } catch (Exception e) {
                    recordExecution(startTime, false);
                    future.complete(ExecutionResult.error("Async execution failed: " + e.getMessage()));
                } finally {
                    activeSessions.remove(session.getId());
                }
            });
            
        } catch (Exception e) {
            recordExecution(startTime, false);
            future.complete(ExecutionResult.error("Failed to schedule async execution: " + e.getMessage()));
        }
        
        return future;
    }
    
    /**
     * Execute script with delay
     */
    private CompletableFuture<ExecutionResult> executeDelayed(ExecutionSession session, 
                                                             CompletableFuture<ExecutionResult> future, long startTime, long delayTicks) {
        try {
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        ExecutionResult result = executeScriptInternal(session);
                        recordExecution(startTime, result.isSuccess());
                        future.complete(result);
                    } catch (Exception e) {
                        recordExecution(startTime, false);
                        future.complete(ExecutionResult.error("Delayed execution failed: " + e.getMessage()));
                    } finally {
                        activeSessions.remove(session.getId());
                    }
                }
            }.runTaskLater(plugin, delayTicks);
            
        } catch (Exception e) {
            recordExecution(startTime, false);
            future.complete(ExecutionResult.error("Failed to schedule delayed execution: " + e.getMessage()));
        }
        
        return future;
    }
    
    /**
     * Execute script in batch mode
     */
    private CompletableFuture<ExecutionResult> executeBatch(ExecutionSession session, 
                                                           CompletableFuture<ExecutionResult> future, long startTime) {
        
        synchronized (priorityQueues.get(Priority.NORMAL)) {
            priorityQueues.get(Priority.NORMAL).add(new ExecutionTask(session, future, startTime));
        }
        
        return future;
    }
    
    /**
     * Execute script based on priority
     */
    private CompletableFuture<ExecutionResult> executePrioritized(ExecutionSession session, 
                                                                 CompletableFuture<ExecutionResult> future, long startTime) {
        synchronized (priorityQueues.get(session.getPriority())) {
            priorityQueues.get(session.getPriority()).add(new ExecutionTask(session, future, startTime));
        }
        
        return future;
    }
    
    /**
     * Internal script execution logic
     */
    private ExecutionResult executeScriptInternal(ExecutionSession session) {
        try {
            
            ExecutionContext context = new ExecutionContext.Builder()
                .plugin(plugin)
                .player(session.getPlayer())
                .creativeWorld(plugin.getServiceRegistry().getWorldManager().findCreativeWorldByBukkit(session.getPlayer().getWorld()))
                .currentBlock(session.getScript().getRootBlock())
                .executionMode(session.getMode())
                .priority(session.getPriority())
                .maxInstructions(getMaxInstructionsForPriority(session.getPriority()))
                .build();
            
            
            context.setExecutionTimeout(System.currentTimeMillis() + MAX_EXECUTION_TIME_MS);
            
            
            return plugin.getServiceRegistry().getService(com.megacreative.coding.ScriptEngine.class)
                .executeScript(session.getScript(), session.getPlayer(), session.getTrigger())
                .get(MAX_EXECUTION_TIME_MS, TimeUnit.MILLISECONDS);
                
        } catch (Exception e) {
            return ExecutionResult.error("Script execution failed: " + e.getMessage());
        }
    }
    
    /**
     * Get maximum instructions allowed based on priority
     */
    private int getMaxInstructionsForPriority(Priority priority) {
        switch (priority) {
            case CRITICAL: return MAX_INSTRUCTIONS_PER_TICK * 2;
            case HIGH: return MAX_INSTRUCTIONS_PER_TICK;
            case NORMAL: return MAX_INSTRUCTIONS_PER_TICK / 2;
            case LOW: return MAX_INSTRUCTIONS_PER_TICK / 4;
            case IDLE: return MAX_INSTRUCTIONS_PER_TICK / 8;
            default: return MAX_INSTRUCTIONS_PER_TICK;
        }
    }
    
    /**
     * Start background processor for batch and prioritized execution
     */
    private void startBackgroundProcessor() {
        new BukkitRunnable() {
            @Override
            public void run() {
                processPriorityQueues();
                processBatchQueues();
                monitor.updateStatistics();
            }
        }.runTaskTimerAsynchronously(plugin, 20L, 20L); 
    }
    
    /**
     * Process priority-based execution queues
     */
    private void processPriorityQueues() {
        for (Priority priority : Priority.values()) {
            List<ExecutionTask> queue = priorityQueues.get(priority);
            if (queue.isEmpty()) continue;
            
            synchronized (queue) {
                int processed = 0;
                int maxToProcess = getMaxTasksForPriority(priority);
                
                while (!queue.isEmpty() && processed < maxToProcess) {
                    ExecutionTask task = queue.remove(0);
                    executeTask(task);
                    processed++;
                }
            }
        }
    }
    
    /**
     * Process batch execution queues
     */
    private void processBatchQueues() {
        List<ExecutionTask> batchQueue = priorityQueues.get(Priority.NORMAL);
        
        synchronized (batchQueue) {
            if (batchQueue.size() >= BATCH_SIZE) {
                List<ExecutionTask> batch = new ArrayList<>();
                for (int i = 0; i < BATCH_SIZE && !batchQueue.isEmpty(); i++) {
                    batch.add(batchQueue.remove(0));
                }
                
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for (ExecutionTask task : batch) {
                            executeTask(task);
                        }
                    }
                }.runTask(plugin);
            }
        }
    }
    
    /**
     * Execute a single task
     */
    private void executeTask(ExecutionTask task) {
        try {
            ExecutionResult result = executeScriptInternal(task.getSession());
            recordExecution(task.getStartTime(), result.isSuccess());
            task.getFuture().complete(result);
        } catch (Exception e) {
            recordExecution(task.getStartTime(), false);
            task.getFuture().complete(ExecutionResult.error("Task execution failed: " + e.getMessage()));
        } finally {
            activeSessions.remove(task.getSession().getId());
        }
    }
    
    /**
     * Get maximum tasks to process per priority level
     */
    private int getMaxTasksForPriority(Priority priority) {
        switch (priority) {
            case CRITICAL: return 20;
            case HIGH: return 15;
            case NORMAL: return 10;
            case LOW: return 5;
            case IDLE: return 2;
            default: return 10;
        }
    }
    
    /**
     * Record execution statistics
     */
    private void recordExecution(long startTime, boolean success) {
        long executionTime = System.currentTimeMillis() - startTime;
        
        if (success) {
            successfulExecutions++;
        } else {
            failedExecutions++;
        }
        
        
        averageExecutionTime = (averageExecutionTime + executionTime) / 2;
        
        monitor.recordExecution(executionTime, success);
    }
    
    /**
     * Get execution statistics
     */
    public ExecutionStatistics getStatistics() {
        return new ExecutionStatistics(
            totalExecutions,
            successfulExecutions,
            failedExecutions,
            averageExecutionTime,
            activeSessions.size(),
            asyncExecutor.getActiveCount(),
            monitor.getThroughput()
        );
    }
    
    /**
     * Cancel all executions for a specific player
     */
    public void cancelPlayerExecutions(Player player) {
        activeSessions.values().removeIf(session -> {
            if (session.getPlayer().getUniqueId().equals(player.getUniqueId())) {
                session.cancel();
                return true;
            }
            return false;
        });
    }
    
    /**
     * Shutdown the execution engine
     */
    public void shutdown() {
        
        activeSessions.values().forEach(ExecutionSession::cancel);
        activeSessions.clear();
        
        
        priorityQueues.values().forEach(List::clear);
        
        
        asyncExecutor.shutdown();
        try {
            if (!asyncExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                asyncExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            asyncExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        plugin.getLogger().fine("🎆 Advanced Execution Engine shutdown complete");
    }
    
    
    
    /**
     * Execution session data
     */
    private static class ExecutionSession {
        private final UUID id;
        private final CodeScript script;
        private final Player player;
        private final ExecutionMode mode;
        private final Priority priority;
        private final String trigger;
        private volatile boolean cancelled = false;
        
        public ExecutionSession(UUID id, CodeScript script, Player player, ExecutionMode mode, Priority priority, String trigger) {
            this.id = id;
            this.script = script;
            this.player = player;
            this.mode = mode;
            this.priority = priority;
            this.trigger = trigger;
        }
        
        
        public UUID getId() { return id; }
        public CodeScript getScript() { return script; }
        public Player getPlayer() { return player; }
        public ExecutionMode getMode() { return mode; }
        public Priority getPriority() { return priority; }
        public String getTrigger() { return trigger; }
        public boolean isCancelled() { return cancelled; }
        
        public void cancel() { this.cancelled = true; }
    }
    
    /**
     * Execution task wrapper
     */
    private static class ExecutionTask {
        private final ExecutionSession session;
        private final CompletableFuture<ExecutionResult> future;
        private final long startTime;
        
        public ExecutionTask(ExecutionSession session, CompletableFuture<ExecutionResult> future, long startTime) {
            this.session = session;
            this.future = future;
            this.startTime = startTime;
        }
        
        public ExecutionSession getSession() { return session; }
        public CompletableFuture<ExecutionResult> getFuture() { return future; }
        public long getStartTime() { return startTime; }
    }
    
    /**
     * Execution monitoring and statistics
     */
    private static class ExecutionMonitor {
        private long lastUpdateTime = System.currentTimeMillis();
        private long executionsLastSecond = 0;
        private double throughput = 0.0;
        
        public void recordExecution(long executionTime, boolean success) {
            executionsLastSecond++;
        }
        
        public void updateStatistics() {
            long currentTime = System.currentTimeMillis();
            long timeDiff = currentTime - lastUpdateTime;
            
            if (timeDiff >= 1000) { 
                throughput = (executionsLastSecond * 1000.0) / timeDiff;
                executionsLastSecond = 0;
                lastUpdateTime = currentTime;
            }
        }
        
        public double getThroughput() {
            return throughput;
        }
    }
    
    /**
     * Execution statistics data structure
     */
    public static class ExecutionStatistics {
        private final long totalExecutions;
        private final long successfulExecutions;
        private final long failedExecutions;
        private final long averageExecutionTime;
        private final int activeSessions;
        private final int activeThreads;
        private final double throughput;
        
        public ExecutionStatistics(long totalExecutions, long successfulExecutions, long failedExecutions,
                                 long averageExecutionTime, int activeSessions, int activeThreads, double throughput) {
            this.totalExecutions = totalExecutions;
            this.successfulExecutions = successfulExecutions;
            this.failedExecutions = failedExecutions;
            this.averageExecutionTime = averageExecutionTime;
            this.activeSessions = activeSessions;
            this.activeThreads = activeThreads;
            this.throughput = throughput;
        }
        
        
        public long getTotalExecutions() { return totalExecutions; }
        public long getSuccessfulExecutions() { return successfulExecutions; }
        public long getFailedExecutions() { return failedExecutions; }
        public long getAverageExecutionTime() { return averageExecutionTime; }
        public int getActiveSessions() { return activeSessions; }
        public int getActiveThreads() { return activeThreads; }
        public double getThroughput() { return throughput; }
        public double getSuccessRate() { 
            return totalExecutions > 0 ? (double) successfulExecutions / totalExecutions * 100 : 0;
        }
    }
}