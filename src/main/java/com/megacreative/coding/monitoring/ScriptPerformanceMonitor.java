package com.megacreative.coding.monitoring;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Advanced performance monitoring system for visual programming scripts
 * Tracks execution times, memory usage, and provides optimization recommendations
 */
public class ScriptPerformanceMonitor {
    
    private final Plugin plugin;
    
    // Performance tracking data structures
    private final Map<UUID, PlayerScriptMetrics> playerMetrics = new ConcurrentHashMap<>();
    private final Map<String, ActionPerformanceData> actionPerformance = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    // Performance thresholds (loaded from config)
    private final long slowExecutionThreshold;
    private final long memoryWarningThreshold;
    private final int maxConcurrentScripts;
    
    public ScriptPerformanceMonitor(Plugin plugin) {
        this.plugin = plugin;
        
        // Load performance thresholds from config
        this.slowExecutionThreshold = plugin.getConfig().getLong("coding.performance.slow_execution_threshold", 50);
        this.memoryWarningThreshold = plugin.getConfig().getLong("coding.performance.memory_warning_threshold", 100 * 1024 * 1024);
        this.maxConcurrentScripts = plugin.getConfig().getInt("coding.max_concurrent_scripts", 20);
    }
    
    /**
     * Starts performance tracking for a script execution
     */
    public ExecutionTracker startTracking(Player player, String scriptName, String actionType) {
        UUID playerId = player.getUniqueId();
        PlayerScriptMetrics metrics = playerMetrics.computeIfAbsent(playerId, k -> new PlayerScriptMetrics());
        
        return new ExecutionTracker(this, player, scriptName, actionType, System.currentTimeMillis());
    }
    
    /**
     * Records the completion of a script execution
     */
    public void recordExecution(Player player, String scriptName, String actionType, 
                               long executionTime, boolean success, String errorMessage) {
        UUID playerId = player.getUniqueId();
        
        // Update player metrics
        PlayerScriptMetrics playerMetrics = this.playerMetrics.get(playerId);
        if (playerMetrics != null) {
            playerMetrics.recordExecution(scriptName, actionType, executionTime, success);
        }
        
        // Update action performance data
        ActionPerformanceData actionData = actionPerformance.computeIfAbsent(actionType, 
            k -> new ActionPerformanceData(actionType));
        actionData.recordExecution(executionTime, success);
        
        // Update global metrics
        totalExecutions.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        
        // Check for performance issues
        checkPerformanceIssues(player, actionType, executionTime, errorMessage);
    }
    
    /**
     * Checks for performance issues and alerts players/admins
     */
    private void checkPerformanceIssues(Player player, String actionType, long executionTime, String errorMessage) {
        // Check for slow execution
        if (executionTime > slowExecutionThreshold) {
            if (player.hasPermission("megacreative.debug")) {
                player.sendMessage("§e⚠ Slow execution detected: " + actionType + 
                                 " took " + executionTime + "ms");
            }
        }
        
        // Check memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        if (usedMemory > memoryWarningThreshold) {
            if (player.hasPermission("megacreative.admin")) {
                player.sendMessage("§c⚠ High memory usage: " + (usedMemory / 1024 / 1024) + "MB");
            }
        }
        
        // Log errors for debugging
        if (errorMessage != null && player.hasPermission("megacreative.debug")) {
            player.sendMessage("§c✗ Script error in " + actionType + ": " + errorMessage);
        }
    }
    
    /**
     * Gets performance statistics for a player
     */
    public PlayerScriptMetrics getPlayerMetrics(UUID playerId) {
        return playerMetrics.get(playerId);
    }
    
    /**
     * Gets performance statistics for an action type
     */
    public ActionPerformanceData getActionPerformance(String actionType) {
        return actionPerformance.get(actionType);
    }
    
    /**
     * Generates a performance report for a player
     */
    public void sendPerformanceReport(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerScriptMetrics metrics = playerMetrics.get(playerId);
        
        if (metrics == null) {
            player.sendMessage("§eNo performance data available yet.");
            return;
        }
        
        player.sendMessage("§a§l=== Script Performance Report ===");
        player.sendMessage("§7Total Executions: §f" + metrics.getTotalExecutions());
        player.sendMessage("§7Success Rate: §f" + String.format("%.1f%%", metrics.getSuccessRate()));
        player.sendMessage("§7Average Execution Time: §f" + String.format("%.2fms", metrics.getAverageExecutionTime()));
        player.sendMessage("§7Fastest Action: §f" + metrics.getFastestAction() + " (" + metrics.getFastestTime() + "ms)");
        player.sendMessage("§7Slowest Action: §f" + metrics.getSlowestAction() + " (" + metrics.getSlowestTime() + "ms)");
        
        // Show top 5 most used actions
        player.sendMessage("§7§l--- Most Used Actions ---");
        metrics.getTopActions(5).forEach((action, count) -> 
            player.sendMessage("§7- " + action + ": §f" + count + " times"));
        
        // Performance recommendations
        generateRecommendations(player, metrics);
    }
    
    /**
     * Generates performance optimization recommendations
     */
    private void generateRecommendations(Player player, PlayerScriptMetrics metrics) {
        player.sendMessage("§e§l--- Optimization Recommendations ---");
        
        if (metrics.getAverageExecutionTime() > 25) {
            player.sendMessage("§e• Consider using async loops for repetitive tasks");
        }
        
        if (metrics.getSuccessRate() < 90) {
            player.sendMessage("§e• Check error conditions in your scripts");
        }
        
        if (metrics.getUniqueActionsUsed() < 10) {
            player.sendMessage("§e• Explore more action types to enhance your scripts");
        }
        
        String slowestAction = metrics.getSlowestAction();
        if (slowestAction != null && metrics.getSlowestTime() > 100) {
            player.sendMessage("§e• Optimize usage of: " + slowestAction);
        }
    }
    
    /**
     * Cleans up metrics for a player (called on disconnect)
     */
    public void cleanupPlayer(UUID playerId) {
        playerMetrics.remove(playerId);
    }
    
    /**
     * Gets global system performance statistics
     */
    public SystemPerformanceReport getSystemReport() {
        return new SystemPerformanceReport(
            totalExecutions.get(),
            totalExecutionTime.get(),
            playerMetrics.size(),
            actionPerformance.size()
        );
    }
    
    /**
     * Execution tracker for measuring individual script performance
     */
    public static class ExecutionTracker implements AutoCloseable {
        private final ScriptPerformanceMonitor monitor;
        private final Player player;
        private final String scriptName;
        private final String actionType;
        private final long startTime;
        private boolean success = true;
        private String errorMessage;
        
        public ExecutionTracker(ScriptPerformanceMonitor monitor, Player player, 
                              String scriptName, String actionType, long startTime) {
            this.monitor = monitor;
            this.player = player;
            this.scriptName = scriptName;
            this.actionType = actionType;
            this.startTime = startTime;
        }
        
        public void markError(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
        }
        
        @Override
        public void close() {
            long executionTime = System.currentTimeMillis() - startTime;
            monitor.recordExecution(player, scriptName, actionType, executionTime, success, errorMessage);
        }
    }
    
    /**
     * Performance metrics for individual players
     */
    public static class PlayerScriptMetrics {
        private final Map<String, ActionMetrics> actionMetrics = new ConcurrentHashMap<>();
        private long totalExecutions = 0;
        private long totalSuccesses = 0;
        private long totalExecutionTime = 0;
        private String fastestAction = null;
        private long fastestTime = Long.MAX_VALUE;
        private String slowestAction = null;
        private long slowestTime = 0;
        
        public void recordExecution(String scriptName, String actionType, long executionTime, boolean success) {
            ActionMetrics metrics = actionMetrics.computeIfAbsent(actionType, ActionMetrics::new);
            metrics.recordExecution(executionTime, success);
            
            totalExecutions++;
            totalExecutionTime += executionTime;
            if (success) totalSuccesses++;
            
            // Track fastest/slowest
            if (executionTime < fastestTime) {
                fastestTime = executionTime;
                fastestAction = actionType;
            }
            if (executionTime > slowestTime) {
                slowestTime = executionTime;
                slowestAction = actionType;
            }
        }
        
        public double getSuccessRate() {
            return totalExecutions > 0 ? (totalSuccesses * 100.0) / totalExecutions : 0;
        }
        
        public double getAverageExecutionTime() {
            return totalExecutions > 0 ? totalExecutionTime / (double) totalExecutions : 0;
        }
        
        public Map<String, Long> getTopActions(int limit) {
            return actionMetrics.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getExecutionCount(), e1.getValue().getExecutionCount()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getExecutionCount(),
                    (e1, e2) -> e1,
                    java.util.LinkedHashMap::new
                ));
        }
        
        // Getters
        public long getTotalExecutions() { return totalExecutions; }
        public String getFastestAction() { return fastestAction; }
        public long getFastestTime() { return fastestTime; }
        public String getSlowestAction() { return slowestAction; }
        public long getSlowestTime() { return slowestTime; }
        public int getUniqueActionsUsed() { return actionMetrics.size(); }
    }
    
    /**
     * Performance data for individual action types
     */
    public static class ActionPerformanceData {
        private final String actionType;
        private long executionCount = 0;
        private long successCount = 0;
        private long totalExecutionTime = 0;
        private long minExecutionTime = Long.MAX_VALUE;
        private long maxExecutionTime = 0;
        
        public ActionPerformanceData(String actionType) {
            this.actionType = actionType;
        }
        
        public void recordExecution(long executionTime, boolean success) {
            executionCount++;
            totalExecutionTime += executionTime;
            if (success) successCount++;
            
            minExecutionTime = Math.min(minExecutionTime, executionTime);
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
        }
        
        public double getAverageExecutionTime() {
            return executionCount > 0 ? totalExecutionTime / (double) executionCount : 0;
        }
        
        public double getSuccessRate() {
            return executionCount > 0 ? (successCount * 100.0) / executionCount : 0;
        }
        
        // Getters
        public String getActionType() { return actionType; }
        public long getExecutionCount() { return executionCount; }
        public long getMinExecutionTime() { return minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
    }
    
    /**
     * Individual action metrics
     */
    private static class ActionMetrics {
        private final String actionType;
        private long executionCount = 0;
        private long successCount = 0;
        private long totalExecutionTime = 0;
        
        public ActionMetrics(String actionType) {
            this.actionType = actionType;
        }
        
        public void recordExecution(long executionTime, boolean success) {
            executionCount++;
            totalExecutionTime += executionTime;
            if (success) successCount++;
        }
        
        public long getExecutionCount() { return executionCount; }
    }
    
    /**
     * System-wide performance report
     */
    public static class SystemPerformanceReport {
        private final long totalExecutions;
        private final long totalExecutionTime;
        private final int activePlayerCount;
        private final int uniqueActionTypes;
        
        public SystemPerformanceReport(long totalExecutions, long totalExecutionTime, 
                                     int activePlayerCount, int uniqueActionTypes) {
            this.totalExecutions = totalExecutions;
            this.totalExecutionTime = totalExecutionTime;
            this.activePlayerCount = activePlayerCount;
            this.uniqueActionTypes = uniqueActionTypes;
        }
        
        public double getAverageExecutionTime() {
            return totalExecutions > 0 ? totalExecutionTime / (double) totalExecutions : 0;
        }
        
        // Getters
        public long getTotalExecutions() { return totalExecutions; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public int getActivePlayerCount() { return activePlayerCount; }
        public int getUniqueActionTypes() { return uniqueActionTypes; }
    }
}