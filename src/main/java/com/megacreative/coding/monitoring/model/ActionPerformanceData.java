package com.megacreative.coding.monitoring.model;

/**
 * Performance data for a specific action
 */
public class ActionPerformanceData {
    private final String actionType;
    private long executionCount = 0;
    private long totalExecutionTime = 0;
    private long lastExecutionTime = 0;
    private long peakExecutionTime = 0;
    private long minExecutionTime = Long.MAX_VALUE;
    private long successCount = 0;
    private long failureCount = 0;
    
    public ActionPerformanceData(String actionType) {
        this.actionType = actionType;
    }
    
    public void recordExecution(long executionTime, boolean success) {
        executionCount++;
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
    
    public String getActionType() {
        return actionType;
    }
    
    public long getExecutionCount() {
        return executionCount;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public double getAverageExecutionTime() {
        return executionCount > 0 ? (double) totalExecutionTime / executionCount : 0.0;
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
        return executionCount > 0 ? (successCount * 100.0) / executionCount : 0.0;
    }
}
