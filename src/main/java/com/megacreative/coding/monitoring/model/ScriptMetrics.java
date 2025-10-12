package com.megacreative.coding.monitoring.model;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Represents metrics for a specific script
 */
public class ScriptMetrics {
    private final String scriptName;
    private final AtomicLong executionCount = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicLong peakExecutionTime = new AtomicLong(0);
    private final AtomicLong minExecutionTime = new AtomicLong(Long.MAX_VALUE);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final Map<String, ActionMetrics> actionMetrics = new ConcurrentHashMap<>();
    private final List<Long> executionTimes = new CopyOnWriteArrayList<>();
    
    public ScriptMetrics(String scriptName) {
        this.scriptName = scriptName;
    }
    
    /**
     * Constructor that creates ScriptMetrics from ScriptPerformanceProfile
     * @param profile The profile to convert from
     */
    public ScriptMetrics(ScriptPerformanceProfile profile) {
        this.scriptName = profile.getScriptName();
        this.executionCount.set(profile.getTotalExecutions());
        this.totalExecutionTime.set(profile.getTotalExecutionTime());
        this.peakExecutionTime.set(profile.getPeakExecutionTime());
        this.minExecutionTime.set(profile.getMinExecutionTime());
        this.successCount.set((long)(profile.getTotalExecutions() * profile.getSuccessRate()));
        this.failureCount.set(profile.getTotalExecutions() - successCount.get());

        
        for (ActionPerformanceData actionData : profile.getAllActionData()) {
            ActionMetrics actionMetrics = new ActionMetrics(actionData.getActionType());
            
            this.actionMetrics.put(actionData.getActionType(), actionMetrics);
        }
    }
    
    /**
     * Creates a new ScriptMetrics instance with pre-populated values
     */
    public ScriptMetrics(long totalExecutions, double averageExecutionTime, 
                        long totalExecutionTime, long lastExecutionTime,
                        double successRate, double errorRate) {
        this.scriptName = "";
        this.executionCount.set(totalExecutions);
        this.totalExecutionTime.set(totalExecutionTime);
        this.peakExecutionTime.set(lastExecutionTime);
        this.successCount.set((long)(totalExecutions * successRate));
        this.failureCount.set((long)(totalExecutions * errorRate));
    }
    
    public void recordExecution(String actionType, long executionTime, boolean success) {
        executionCount.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        
        if (executionTime > peakExecutionTime.get()) {
            peakExecutionTime.set(executionTime);
        }
        
        if (executionTime < minExecutionTime.get()) {
            minExecutionTime.set(executionTime);
        }
        
        if (success) {
            successCount.incrementAndGet();
        } else {
            failureCount.incrementAndGet();
        }
        
        actionMetrics.computeIfAbsent(actionType, ActionMetrics::new)
                   .recordExecution(executionTime, success);
        
        executionTimes.add(executionTime);
        if (executionTimes.size() > 1000) {
            executionTimes.remove(0);
        }
    }
    
    public String getScriptName() {
        return scriptName;
    }
    
    public long getExecutionCount() {
        return executionCount.get();
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }
    
    public double getAverageExecutionTime() {
        return executionCount.get() > 0 
            ? (double) totalExecutionTime.get() / executionCount.get() 
            : 0.0;
    }
    
    public long getPeakExecutionTime() {
        return peakExecutionTime.get();
    }
    
    public long getMinExecutionTime() {
        return minExecutionTime.get() == Long.MAX_VALUE ? 0 : minExecutionTime.get();
    }
    
    public double getSuccessRate() {
        return executionCount.get() > 0 
            ? (successCount.get() * 100.0) / executionCount.get() 
            : 0.0;
    }
    
    public List<Long> getExecutionTimes() {
        return new ArrayList<>(executionTimes);
    }
    
    public Collection<ActionMetrics> getActionMetrics() {
        return actionMetrics.values();
    }
    
    public ActionMetrics getActionMetrics(String actionType) {
        return actionMetrics.get(actionType);
    }
    
    /**
     * Gets the error rate as a decimal (0.0 to 1.0)
     * @return Error rate
     */
    public double getErrorRate() {
        return executionCount.get() > 0 
            ? (double) failureCount.get() / executionCount.get() 
            : 0.0;
    }
}
