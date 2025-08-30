package com.megacreative.coding.monitoring.model;

import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import org.bukkit.entity.Player;

/**
 * Tracks an individual script execution
 */
public class ExecutionTracker implements AutoCloseable {
    private final ScriptPerformanceMonitor monitor;
    private final Player player;
    private final String scriptName;
    private final String actionType;
    private final long startTime;
    private final ScriptPerformanceProfile profile;
    private boolean success = true;
    private String errorMessage;

    public ExecutionTracker(ScriptPerformanceMonitor monitor, Player player, 
                          String scriptName, String actionType, 
                          long startTime, ScriptPerformanceProfile profile) {
        this.monitor = monitor;
        this.player = player;
        this.scriptName = scriptName;
        this.actionType = actionType;
        this.startTime = startTime;
        this.profile = profile;
    }
    
    @Override
    public void close() {
        long executionTime = System.currentTimeMillis() - startTime;
        monitor.recordExecution(player, scriptName, actionType, executionTime, success, errorMessage);
    }
    
    public void recordSuccess() {
        this.success = true;
    }
    
    public void recordFailure(String errorMessage) {
        this.success = false;
        this.errorMessage = errorMessage;
    }
    
    /**
     * Marks the execution as failed with the given error message
     * @param errorMessage The error message to record
     */
    public void markError(String errorMessage) {
        recordFailure(errorMessage);
    }
}
