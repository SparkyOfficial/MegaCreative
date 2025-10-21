package com.megacreative.coding.monitoring;

import com.megacreative.coding.CodeScript;
import com.megacreative.coding.monitoring.model.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;


/**
 * Advanced performance monitoring system for visual programming scripts
 * Tracks execution times, memory usage, and provides optimization recommendations
 */
public class ScriptPerformanceMonitor {
    private final Plugin plugin;
    private final long startTime;
    
    // These fields need to be class fields to maintain state across method calls
    // Convert initialization tracking fields to local variables where possible
    // These fields need to remain as class fields since they track performance data across method calls
    private final Map<UUID, PlayerScriptMetrics> playerMetrics = new ConcurrentHashMap<>();
    private final Map<String, ActionPerformanceData> actionPerformance = new ConcurrentHashMap<>();
    private final Map<String, ScriptPerformanceProfile> scriptProfiles = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    

    public static class ExecutionTracker implements AutoCloseable {
        private final ScriptPerformanceMonitor monitor;
        private final String action;
        private final ScriptPerformanceProfile profile;
        private final long startTime;
        private boolean completed = false;
        
        private ExecutionTracker(ScriptPerformanceMonitor monitor, Player player, 
                               String scriptName, String action, long startTime,
                               ScriptPerformanceProfile profile) {
            this.monitor = monitor;
            this.action = action;
            this.profile = profile;
            this.startTime = startTime;
        }
        
        @Override
        public void close() {
            if (!completed) {
                long endTime = System.currentTimeMillis();
                long executionTime = endTime - startTime;
                
                // Update metrics
                monitor.totalExecutions.incrementAndGet();
                monitor.totalExecutionTime.addAndGet(executionTime);
                
                completed = true;
            }
        }
    }
    
    public ScriptPerformanceMonitor(Plugin plugin) {
        this.plugin = plugin;
        this.startTime = System.currentTimeMillis();
        
        MemoryMonitor memoryMonitor = new MemoryMonitor();
        ExecutionSampler executionSampler = new ExecutionSampler();
        executionSampler.start();
        memoryMonitor.start();
        
        BottleneckDetector bottleneckDetector = new BottleneckDetector();
        bottleneckDetector.start();
    }
    
    /**
     * Records a script execution
     * @param player The player who triggered the execution (can be null for console)
     * @param scriptName The name of the script being executed
     * @param actionType The type of action being performed
     * @param executionTime The time taken to execute the script in milliseconds
     * @param success Whether the execution was successful
     * @param errorMessage Error message if execution failed, null otherwise
     */
    public void recordExecution(Player player, String scriptName, String actionType, 
                               long executionTime, boolean success, String errorMessage) {
        if (player != null) {
            
            PlayerScriptMetrics metrics = playerMetrics.computeIfAbsent(
                player.getUniqueId(), PlayerScriptMetrics::new);
            metrics.recordExecution(scriptName, actionType, executionTime, success);
            
            
            actionPerformance.computeIfAbsent(actionType, k -> new ActionPerformanceData(actionType))
                .recordExecution(executionTime, success);
            
            
            ScriptPerformanceProfile profile = scriptProfiles.computeIfAbsent(
                scriptName, ScriptPerformanceProfile::new);
            profile.recordExecution(actionType, executionTime, success);
            
            
            ExecutionSampler executionSampler = new ExecutionSampler();
            executionSampler.recordExecution(scriptName, actionType, executionTime);
            
            
            List<ScriptMetrics> metricsList = new ArrayList<>();
            for (ScriptPerformanceProfile scriptProfile : scriptProfiles.values()) {
                metricsList.add(new ScriptMetrics(scriptProfile));
            }
            BottleneckDetector bottleneckDetector = new BottleneckDetector();
            bottleneckDetector.detectBottlenecks(metricsList);
            
            

        }
    }
    
    /**
     * Creates a new execution tracker for measuring script execution time
     * @param player The player executing the script (can be null for console)
     * @param script The script being executed
     * @param actionType The type of action being performed
     * @return An AutoCloseable execution tracker
     */
    public ExecutionTracker trackExecution(Player player, CodeScript script, String actionType) {
        String scriptName = script != null ? script.getName() : "unknown";
        ScriptPerformanceProfile profile = scriptProfiles.computeIfAbsent(
            scriptName, ScriptPerformanceProfile::new);
            
        return new ExecutionTracker(this, player, scriptName, actionType, 
                                  System.currentTimeMillis(), profile);
    }
    
    /**
     * Starts tracking a script or block execution
     * @param player The player who triggered the execution
     * @param scriptId The ID of the script or block being executed
     * @param action The action being performed
     * @return An ExecutionTracker for the execution
     */
    public ExecutionTracker startTracking(Player player, String scriptId, String action) {
        String scriptName = scriptId != null ? scriptId : "unknown";
        ScriptPerformanceProfile profile = scriptProfiles.computeIfAbsent(
            scriptName, ScriptPerformanceProfile::new);
            
        return new ExecutionTracker(this, player, scriptName, action, 
                                  System.currentTimeMillis(), profile);
    }
    
    /**
     * Gets performance metrics for a specific player
     * @param playerId The player's UUID
     * @return Player metrics or null if not found
     */
    public PlayerScriptMetrics getPlayerMetrics(UUID playerId) {
        return playerMetrics.get(playerId);
    }
    
    /**
     * Gets performance data for a specific script
     * @param scriptName The name of the script
     * @return Script performance profile or null if not found
     */
    public ScriptPerformanceProfile getScriptProfile(String scriptName) {
        return scriptProfiles.get(scriptName);
    }
    
    /**
     * Gets performance data for all scripts, converting it to the ScriptMetrics type.
     * @return Collection of all script performance metrics.
     */
    public Collection<ScriptMetrics> getAllScriptProfiles() {
        List<ScriptMetrics> metricsList = new ArrayList<>();
        for (ScriptPerformanceProfile profile : scriptProfiles.values()) {
            metricsList.add(new ScriptMetrics(profile));
        }
        return metricsList;
    }
    
    /**
     * Gets a system-wide performance report
     * @return System performance report
     */
    public SystemPerformanceReport getSystemPerformanceReport() {
        MemoryMonitor memoryMonitor = new MemoryMonitor();
        GarbageCollectionMonitor gcMonitor = new GarbageCollectionMonitor();
        BottleneckDetector bottleneckDetector = new BottleneckDetector();
        
        return new SystemPerformanceReport(
            totalExecutions.get(),
            totalExecutionTime.get(),
            playerMetrics.size(),
            scriptProfiles.size(),
            memoryMonitor.getCurrentUsage(),
            gcMonitor.getCurrentStatistics(),
            bottleneckDetector.getBottlenecks(),
            System.currentTimeMillis() - startTime
        );
    }
    
    /**
     * Clears all performance data
     */
    public void clearData() {
        playerMetrics.clear();
        actionPerformance.clear();
        scriptProfiles.clear();
        totalExecutions.set(0);
        totalExecutionTime.set(0);
    }
    
    /**
     * Shuts down the performance monitor and cleans up resources
     */
    public void shutdown() {
        ExecutionSampler executionSampler = new ExecutionSampler();
        MemoryMonitor memoryMonitor = new MemoryMonitor();
        BottleneckDetector bottleneckDetector = new BottleneckDetector();
        
        executionSampler.stop();
        memoryMonitor.stop();
        bottleneckDetector.stop();
    }
    
    /**
     * Gets the uptime of the performance monitor in milliseconds
     * @return Uptime in milliseconds
     */
    public long getUptimeMs() {
        return System.currentTimeMillis() - startTime;
    }
}