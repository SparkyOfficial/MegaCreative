package com.megacreative.coding.monitoring.model;

import java.util.*;
import java.util.concurrent.*;

/**
 * Performance profile for a script
 */
public class ScriptPerformanceProfile {
    private final String scriptName;
    private final Map<String, ActionPerformanceData> actionData = new ConcurrentHashMap<>();
    private long totalExecutions = 0;
    private long totalExecutionTime = 0;
    private long lastExecutionTime = 0;
    private long peakExecutionTime = 0;
    private long minExecutionTime = Long.MAX_VALUE;
    private long successCount = 0;
    private long failureCount = 0;
    
    public ScriptPerformanceProfile(String scriptName) {
        this.scriptName = scriptName;
    }
    
    public void recordExecution(String actionType, long executionTime, boolean success) {
        actionData.computeIfAbsent(actionType, ActionPerformanceData::new)
                .recordExecution(executionTime, success);
        
        totalExecutions++;
        totalExecutionTime += executionTime;
        lastExecutionTime = System.currentTimeMillis();
        
        if (executionTime > peakExecutionTime) {
            peakExecutionTime = executionTime;
        }
        
        if (executionTime < minExecutionTime) {
            minExecutionTime = executionTime;
        }
        
        if (success) {
            successCount++;
        } else {
            failureCount++;
        }
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public long getTotalExecutions() {
        return totalExecutions;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public double getAverageExecutionTime() {
        return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
    }
    
    public long getLastExecutionTime() {
        return lastExecutionTime;
    }
    
    public long getPeakExecutionTime() {
        return peakExecutionTime;
    }
    
    public long getMinExecutionTime() {
        return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }
    
    public double getSuccessRate() {
        return totalExecutions > 0 ? (successCount * 100.0) / totalExecutions : 0.0;
    }
    
    public Collection<ActionPerformanceData> getActionData() {
        return actionData.values();
    }
    
    public ActionPerformanceData getActionData(String actionType) {
        return actionData.get(actionType);
    }
}
