package com.megacreative.coding.monitoring;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.monitoring.model.*;
import com.megacreative.coding.monitoring.model.ExecutionSampler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.logging.Logger;

/**
 * Advanced performance monitoring system for visual programming scripts
 * Tracks execution times, memory usage, and provides optimization recommendations
 */
public class ScriptPerformanceMonitor {
    private static final Logger log = Logger.getLogger(ScriptPerformanceMonitor.class.getName());
    
    private final Plugin plugin;
    private final long startTime;
    
    // Performance tracking data structures
    private final Map<UUID, PlayerScriptMetrics> playerMetrics = new ConcurrentHashMap<>();
    private final Map<String, ActionPerformanceData> actionPerformance = new ConcurrentHashMap<>();
    private final Map<String, ScriptPerformanceProfile> scriptProfiles = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    // Advanced monitoring features
    private final com.megacreative.coding.monitoring.model.ExecutionSampler executionSampler = 
        new com.megacreative.coding.monitoring.model.ExecutionSampler();
    private final MemoryMonitor memoryMonitor;
    private final BottleneckDetector bottleneckDetector = new BottleneckDetector();
    
    // Performance thresholds (loaded from config)
    private final long slowExecutionThreshold;
    private final long memoryWarningThreshold;
    private final int maxConcurrentScripts;
    
    /**
     * Tracks the execution of a single script operation
     */
    public static class ExecutionTracker extends com.megacreative.coding.monitoring.model.ExecutionTracker {
        private final ScriptPerformanceMonitor monitor;
        private final String action;
        private final ScriptPerformanceProfile profile;
        private boolean completed = false;
        
        private ExecutionTracker(ScriptPerformanceMonitor monitor, Player player, 
                               String scriptName, String action, long startTime,
                               ScriptPerformanceProfile profile) {
            super(monitor, player, scriptName, action, startTime, profile);
            this.monitor = monitor;
            this.action = action;
            this.profile = profile;
        }
        
        @Override
        public void close() {
            if (!completed) {
                super.close();
                completed = true;
            }
        }
    }
    
    public ScriptPerformanceMonitor(Plugin plugin) {
        this.plugin = plugin;
        this.startTime = System.currentTimeMillis();
        
        // Load performance thresholds from config
        this.slowExecutionThreshold = plugin.getConfig().getLong("coding.performance.slow_execution_threshold", 50);
        this.memoryWarningThreshold = plugin.getConfig().getLong("coding.performance.memory_warning_threshold", 100 * 1024 * 1024);
        this.maxConcurrentScripts = plugin.getConfig().getInt("coding.max_concurrent_scripts", 20);
        
        // Initialize memory monitor with logger
        this.memoryMonitor = new MemoryMonitor(plugin.getLogger());
        
        // Start monitoring services
        executionSampler.start();
        memoryMonitor.start();
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
            // Update player metrics
            PlayerScriptMetrics metrics = playerMetrics.computeIfAbsent(
                player.getUniqueId(), PlayerScriptMetrics::new);
            metrics.recordExecution(scriptName, actionType, executionTime, success);
            
            // Update global metrics
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);
            
            // Update action performance data
            actionPerformance.computeIfAbsent(actionType, k -> new ActionPerformanceData(actionType))
                .recordExecution(executionTime, success);
            
            // Update script profile with success status
            ScriptPerformanceProfile profile = scriptProfiles.computeIfAbsent(
                scriptName, ScriptPerformanceProfile::new);
            profile.recordExecution(actionType, executionTime, success);
            
            // Sample execution for pattern detection
            executionSampler.recordExecution(scriptName, actionType, executionTime);
            
            // Convert ScriptPerformanceProfile to ScriptMetrics and check for bottlenecks
            List<ScriptMetrics> metricsList = new ArrayList<>();
            for (ScriptPerformanceProfile scriptProfile : scriptProfiles.values()) {
                metricsList.add(new ScriptMetrics(scriptProfile));
            }
            bottleneckDetector.detectBottlenecks(metricsList);
            
            // Log slow executions
            if (executionTime > slowExecutionThreshold) {
                log.warning(String.format("Slow execution detected: %s/%s took %dms (threshold: %dms)", 
                    scriptName, actionType, executionTime, slowExecutionThreshold));
            }
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
     * @param id The ID of the script or block being executed
     * @param action The action being performed
     * @return An ExecutionTracker for the execution
     */
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
     * Gets performance data for all scripts
     * @return Collection of script performance profiles
     */
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
        return new SystemPerformanceReport(
            totalExecutions.get(),
            totalExecutionTime.get(),
            playerMetrics.size(),
            scriptProfiles.size(),
            memoryMonitor.getCurrentUsage(),
            memoryMonitor.getGcStatistics(),
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