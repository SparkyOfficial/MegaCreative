package com.megacreative.coding.monitoring.model;

/**
 * Represents an execution pattern for sampling
 */
public class ExecutionPattern {
    private final String scriptName;
    private final String actionType;
    private int frequency = 0;
    private long totalExecutionTime = 0;
    private long lastExecutionTime = 0;
    
    public ExecutionPattern(String scriptName, String actionType) {
        this.scriptName = scriptName;
        this.actionType = actionType;
    }
    
    public void recordExecution(long executionTime) {
        frequency++;
        totalExecutionTime += executionTime;
        lastExecutionTime = System.currentTimeMillis();
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public String getActionType() {
        return actionType;
    }
    
    public int getFrequency() {
        return frequency;
    }
    
    public double getAverageExecutionTime() {
        return frequency > 0 ? (double) totalExecutionTime / frequency : 0.0;
    }
    
    public long getLastExecutionTime() {
        return lastExecutionTime;
    }
}
