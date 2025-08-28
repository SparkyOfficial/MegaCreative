package com.megacreative.coding.monitoring;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Advanced performance monitoring system for visual programming scripts
 * Tracks execution times, memory usage, and provides optimization recommendations
 */
public class ScriptPerformanceMonitor {
    
    private final Plugin plugin;
    
    // Performance tracking data structures
    private final Map<UUID, PlayerScriptMetrics> playerMetrics = new ConcurrentHashMap<>();
    private final Map<String, ActionPerformanceData> actionPerformance = new ConcurrentHashMap<>();
    private final Map<String, ScriptPerformanceProfile> scriptProfiles = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    // Advanced monitoring features
    private final ExecutionSampler executionSampler = new ExecutionSampler();
    private final MemoryMonitor memoryMonitor = new MemoryMonitor();
    private final BottleneckDetector bottleneckDetector = new BottleneckDetector();
    
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
     * Starts performance tracking for a script execution with profiling
     */
    public ExecutionTracker startTracking(Player player, String scriptName, String actionType) {
        UUID playerId = player.getUniqueId();
        PlayerScriptMetrics metrics = playerMetrics.computeIfAbsent(playerId, k -> new PlayerScriptMetrics());
        
        // Create or get script profile
        ScriptPerformanceProfile profile = scriptProfiles.computeIfAbsent(scriptName, 
            k -> new ScriptPerformanceProfile(scriptName));
        
        return new ExecutionTracker(this, player, scriptName, actionType, System.currentTimeMillis(), profile);
    }
    
    /**
     * Records the completion of a script execution with advanced profiling
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
        
        // Update script profile
        ScriptPerformanceProfile profile = scriptProfiles.get(scriptName);
        if (profile != null) {
            profile.recordExecution(actionType, executionTime, success);
        }
        
        // Update global metrics
        totalExecutions.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        
        // Sample execution for detailed analysis
        executionSampler.sampleExecution(scriptName, actionType, executionTime, success);
        
        // Check for bottlenecks
        bottleneckDetector.analyzeExecution(scriptName, actionType, executionTime);
        
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
     * Gets or creates a script performance profile
     */
    public ScriptPerformanceProfile getScriptProfile(String scriptName) {
        return scriptProfiles.computeIfAbsent(scriptName, ScriptPerformanceProfile::new);
    }
    
    /**
     * Gets all script profiles
     */
    public Map<String, ScriptPerformanceProfile> getAllScriptProfiles() {
        return new HashMap<>(scriptProfiles);
    }
    
    /**
     * Generates an advanced performance report for a player
     */
    public void sendAdvancedPerformanceReport(Player player) {
        UUID playerId = player.getUniqueId();
        PlayerScriptMetrics metrics = playerMetrics.get(playerId);
        
        if (metrics == null) {
            player.sendMessage("§eNo performance data available yet.");
            return;
        }
        
        player.sendMessage("§a§l=== Advanced Performance Report ===");
        player.sendMessage("§7Total Executions: §f" + metrics.getTotalExecutions());
        player.sendMessage("§7Success Rate: §f" + String.format("%.1f%%", metrics.getSuccessRate()));
        player.sendMessage("§7Average Execution Time: §f" + String.format("%.2fms", metrics.getAverageExecutionTime()));
        player.sendMessage("§7Fastest Action: §f" + metrics.getFastestAction() + " (" + metrics.getFastestTime() + "ms)");
        player.sendMessage("§7Slowest Action: §f" + metrics.getSlowestAction() + " (" + metrics.getSlowestTime() + "ms)");
        
        // Memory usage
        MemoryUsage memoryUsage = memoryMonitor.getCurrentUsage();
        player.sendMessage("§7Memory Usage: §f" + memoryUsage.getUsedMemoryMB() + "MB / " + memoryUsage.getMaxMemoryMB() + "MB");
        
        // Show top 5 most used actions
        player.sendMessage("§7§l--- Most Used Actions ---");
        metrics.getTopActions(5).forEach((action, count) -> 
            player.sendMessage("§7- " + action + ": §f" + count + " times"));
        
        // Show slowest actions
        player.sendMessage("§7§l--- Slowest Actions ---");
        metrics.getSlowestActions(5).forEach((action, time) -> 
            player.sendMessage("§7- " + action + ": §f" + time + "ms"));
        
        // Show bottlenecks
        player.sendMessage("§7§l--- Detected Bottlenecks ---");
        bottleneckDetector.getTopBottlenecks(3).forEach(bottleneck -> 
            player.sendMessage("§7- " + bottleneck.getActionType() + ": §f" + bottleneck.getAverageTime() + "ms avg"));
        
        // Performance recommendations
        generateAdvancedRecommendations(player, metrics);
    }
    
    /**
     * Generates advanced performance optimization recommendations
     */
    private void generateAdvancedRecommendations(Player player, PlayerScriptMetrics metrics) {
        player.sendMessage("§e§l--- Advanced Optimization Recommendations ---");
        
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
        
        // Advanced recommendations based on sampling
        ExecutionPattern pattern = executionSampler.getMostCommonPattern();
        if (pattern != null && pattern.getFrequency() > 10) {
            player.sendMessage("§e• Detected pattern: " + pattern.getDescription() + 
                             " (occurs " + pattern.getFrequency() + " times)");
        }
        
        // Memory recommendations
        MemoryUsage memoryUsage = memoryMonitor.getCurrentUsage();
        if (memoryUsage.getUsagePercentage() > 80) {
            player.sendMessage("§e• Memory usage is high: " + memoryUsage.getUsagePercentage() + "%");
            player.sendMessage("§e• Consider reducing variable scope or using cleanup actions");
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
     * Gets global system performance statistics with advanced metrics
     */
    public AdvancedSystemPerformanceReport getAdvancedSystemReport() {
        return new AdvancedSystemPerformanceReport(
            totalExecutions.get(),
            totalExecutionTime.get(),
            playerMetrics.size(),
            actionPerformance.size(),
            scriptProfiles.size(),
            memoryMonitor.getCurrentUsage(),
            bottleneckDetector.getDetectedBottlenecks()
        );
    }
    
    /**
     * Execution tracker for measuring individual script performance with profiling
     */
    public static class ExecutionTracker implements AutoCloseable {
        private final ScriptPerformanceMonitor monitor;
        private final Player player;
        private final String scriptName;
        private final String actionType;
        private final long startTime;
        private final ScriptPerformanceProfile profile;
        private boolean success = true;
        private String errorMessage;
        
        public ExecutionTracker(ScriptPerformanceMonitor monitor, Player player, 
                              String scriptName, String actionType, long startTime,
                              ScriptPerformanceProfile profile) {
            this.monitor = monitor;
            this.player = player;
            this.scriptName = scriptName;
            this.actionType = actionType;
            this.startTime = startTime;
            this.profile = profile;
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
     * Enhanced PlayerScriptMetrics with additional analysis capabilities
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
        
        public Map<String, Long> getSlowestActions(int limit) {
            return actionMetrics.entrySet().stream()
                .sorted((e1, e2) -> Long.compare(e2.getValue().getTotalExecutionTime(), e1.getValue().getTotalExecutionTime()))
                .limit(limit)
                .collect(java.util.stream.Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().getAverageExecutionTime(),
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
        public Map<String, ActionMetrics> getActionMetrics() { return new HashMap<>(actionMetrics); }
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
     * Individual action metrics with enhanced tracking
     */
    private static class ActionMetrics {
        private final String actionType;
        private long executionCount = 0;
        private long successCount = 0;
        private long totalExecutionTime = 0;
        private long minExecutionTime = Long.MAX_VALUE;
        private long maxExecutionTime = 0;
        
        public ActionMetrics(String actionType) {
            this.actionType = actionType;
        }
        
        public void recordExecution(long executionTime, boolean success) {
            executionCount++;
            totalExecutionTime += executionTime;
            if (success) successCount++;
            
            minExecutionTime = Math.min(minExecutionTime, executionTime);
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
        }
        
        public long getExecutionCount() { return executionCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getAverageExecutionTime() { return executionCount > 0 ? totalExecutionTime / executionCount : 0; }
        public long getMinExecutionTime() { return minExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
    }
    
    /**
     * Script performance profile for detailed analysis
     */
    public static class ScriptPerformanceProfile {
        private final String scriptName;
        private final Map<String, ActionMetrics> actionMetrics = new ConcurrentHashMap<>();
        private long totalExecutions = 0;
        private long totalExecutionTime = 0;
        private long firstExecutionTime = 0;
        private long lastExecutionTime = 0;
        private final List<Long> executionTimes = new ArrayList<>();
        
        public ScriptPerformanceProfile(String scriptName) {
            this.scriptName = scriptName;
        }
        
        public void recordExecution(String actionType, long executionTime, boolean success) {
            ActionMetrics metrics = actionMetrics.computeIfAbsent(actionType, ActionMetrics::new);
            metrics.recordExecution(executionTime, success);
            
            totalExecutions++;
            totalExecutionTime += executionTime;
            
            if (firstExecutionTime == 0) {
                firstExecutionTime = System.currentTimeMillis();
            }
            lastExecutionTime = System.currentTimeMillis();
            
            // Keep last 100 execution times for trend analysis
            executionTimes.add(executionTime);
            if (executionTimes.size() > 100) {
                executionTimes.remove(0);
            }
        }
        
        public double getAverageExecutionTime() {
            return totalExecutions > 0 ? totalExecutionTime / (double) totalExecutions : 0;
        }
        
        public boolean isImproving() {
            if (executionTimes.size() < 10) return false;
            
            // Simple trend analysis - compare first half vs second half
            int midPoint = executionTimes.size() / 2;
            double firstHalfAvg = executionTimes.subList(0, midPoint).stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
            
            double secondHalfAvg = executionTimes.subList(midPoint, executionTimes.size()).stream()
                .mapToLong(Long::longValue)
                .average()
                .orElse(0);
            
            return secondHalfAvg < firstHalfAvg;
        }
        
        // Getters
        public String getScriptName() { return scriptName; }
        public long getTotalExecutions() { return totalExecutions; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getFirstExecutionTime() { return firstExecutionTime; }
        public long getLastExecutionTime() { return lastExecutionTime; }
        public List<Long> getExecutionTimes() { return new ArrayList<>(executionTimes); }
        public Map<String, ActionMetrics> getActionMetrics() { return new HashMap<>(actionMetrics); }
    }
    
    /**
     * Execution sampler for pattern detection
     */
    public static class ExecutionSampler {
        private final Map<String, ExecutionPattern> patterns = new ConcurrentHashMap<>();
        private final int maxSamples = 1000;
        private int sampleCount = 0;
        
        public void sampleExecution(String scriptName, String actionType, long executionTime, boolean success) {
            if (sampleCount >= maxSamples) return;
            
            sampleCount++;
            
            // Create pattern key
            String patternKey = scriptName + ":" + actionType;
            ExecutionPattern pattern = patterns.computeIfAbsent(patternKey, 
                k -> new ExecutionPattern(scriptName, actionType));
            pattern.recordExecution(executionTime, success);
        }
        
        public ExecutionPattern getMostCommonPattern() {
            return patterns.values().stream()
                .max(Comparator.comparingInt(ExecutionPattern::getFrequency))
                .orElse(null);
        }
        
        public Collection<ExecutionPattern> getAllPatterns() {
            return new ArrayList<>(patterns.values());
        }
    }
    
    /**
     * Represents an execution pattern for analysis
     */
    public static class ExecutionPattern {
        private final String scriptName;
        private final String actionType;
        private int frequency = 0;
        private long totalExecutionTime = 0;
        private int successCount = 0;
        
        public ExecutionPattern(String scriptName, String actionType) {
            this.scriptName = scriptName;
            this.actionType = actionType;
        }
        
        public void recordExecution(long executionTime, boolean success) {
            frequency++;
            totalExecutionTime += executionTime;
            if (success) successCount++;
        }
        
        public double getAverageTime() {
            return frequency > 0 ? totalExecutionTime / (double) frequency : 0;
        }
        
        public double getSuccessRate() {
            return frequency > 0 ? (successCount * 100.0) / frequency : 0;
        }
        
        public String getDescription() {
            return scriptName + " -> " + actionType;
        }
        
        // Getters
        public String getScriptName() { return scriptName; }
        public String getActionType() { return actionType; }
        public int getFrequency() { return frequency; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public int getSuccessCount() { return successCount; }
    }
    
    /**
     * Memory monitor for tracking resource usage
     */
    public static class MemoryMonitor {
        public MemoryUsage getCurrentUsage() {
            Runtime runtime = Runtime.getRuntime();
            long maxMemory = runtime.maxMemory();
            long totalMemory = runtime.totalMemory();
            long freeMemory = runtime.freeMemory();
            long usedMemory = totalMemory - freeMemory;
            
            return new MemoryUsage(usedMemory, maxMemory, totalMemory);
        }
    }
    
    /**
     * Memory usage data
     */
    public static class MemoryUsage {
        private final long usedMemory;
        private final long maxMemory;
        private final long totalMemory;
        
        public MemoryUsage(long usedMemory, long maxMemory, long totalMemory) {
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.totalMemory = totalMemory;
        }
        
        public double getUsagePercentage() {
            return maxMemory > 0 ? (usedMemory * 100.0) / maxMemory : 0;
        }
        
        public long getUsedMemoryMB() {
            return usedMemory / (1024 * 1024);
        }
        
        public long getMaxMemoryMB() {
            return maxMemory / (1024 * 1024);
        }
        
        public long getTotalMemoryMB() {
            return totalMemory / (1024 * 1024);
        }
        
        // Getters
        public long getUsedMemory() { return usedMemory; }
        public long getMaxMemory() { return maxMemory; }
        public long getTotalMemory() { return totalMemory; }
    }
    
    /**
     * Bottleneck detector for identifying performance issues
     */
    public static class BottleneckDetector {
        private final Map<String, Bottleneck> bottlenecks = new ConcurrentHashMap<>();
        
        public void analyzeExecution(String scriptName, String actionType, long executionTime) {
            if (executionTime > 50) { // Only analyze slow executions
                String key = scriptName + ":" + actionType;
                Bottleneck bottleneck = bottlenecks.computeIfAbsent(key, 
                    k -> new Bottleneck(scriptName, actionType));
                bottleneck.recordExecution(executionTime);
            }
        }
        
        public List<Bottleneck> getTopBottlenecks(int limit) {
            return bottlenecks.values().stream()
                .sorted(Comparator.comparingDouble(Bottleneck::getAverageTime).reversed())
                .limit(limit)
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
        }
        
        public Collection<Bottleneck> getDetectedBottlenecks() {
            return new ArrayList<>(bottlenecks.values());
        }
    }
    
    /**
     * Represents a performance bottleneck
     */
    public static class Bottleneck {
        private final String scriptName;
        private final String actionType;
        private int occurrenceCount = 0;
        private long totalExecutionTime = 0;
        private long maxExecutionTime = 0;
        
        public Bottleneck(String scriptName, String actionType) {
            this.scriptName = scriptName;
            this.actionType = actionType;
        }
        
        public void recordExecution(long executionTime) {
            occurrenceCount++;
            totalExecutionTime += executionTime;
            maxExecutionTime = Math.max(maxExecutionTime, executionTime);
        }
        
        public double getAverageTime() {
            return occurrenceCount > 0 ? totalExecutionTime / (double) occurrenceCount : 0;
        }
        
        // Getters
        public String getScriptName() { return scriptName; }
        public String getActionType() { return actionType; }
        public int getOccurrenceCount() { return occurrenceCount; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public long getMaxExecutionTime() { return maxExecutionTime; }
    }
    
    /**
     * Advanced system performance report with detailed metrics
     */
    public static class AdvancedSystemPerformanceReport {
        private final long totalExecutions;
        private final long totalExecutionTime;
        private final int activePlayerCount;
        private final int uniqueActionTypes;
        private final int scriptProfilesCount;
        private final MemoryUsage memoryUsage;
        private final Collection<Bottleneck> bottlenecks;
        
        public AdvancedSystemPerformanceReport(long totalExecutions, long totalExecutionTime, 
                                             int activePlayerCount, int uniqueActionTypes,
                                             int scriptProfilesCount, MemoryUsage memoryUsage,
                                             Collection<Bottleneck> bottlenecks) {
            this.totalExecutions = totalExecutions;
            this.totalExecutionTime = totalExecutionTime;
            this.activePlayerCount = activePlayerCount;
            this.uniqueActionTypes = uniqueActionTypes;
            this.scriptProfilesCount = scriptProfilesCount;
            this.memoryUsage = memoryUsage;
            this.bottlenecks = bottlenecks;
        }
        
        public double getAverageExecutionTime() {
            return totalExecutions > 0 ? totalExecutionTime / (double) totalExecutions : 0;
        }
        
        public List<Bottleneck> getTopBottlenecks(int limit) {
            return bottlenecks.stream()
                .sorted(Comparator.comparingDouble(Bottleneck::getAverageTime).reversed())
                .limit(limit)
                .collect(ArrayList::new, (list, item) -> list.add(item), (list1, list2) -> list1.addAll(list2));
        }
        
        // Getters
        public long getTotalExecutions() { return totalExecutions; }
        public long getTotalExecutionTime() { return totalExecutionTime; }
        public int getActivePlayerCount() { return activePlayerCount; }
        public int getUniqueActionTypes() { return uniqueActionTypes; }
        public int getScriptProfilesCount() { return scriptProfilesCount; }
        public MemoryUsage getMemoryUsage() { return memoryUsage; }
        public Collection<Bottleneck> getBottlenecks() { return bottlenecks; }
    }
}