package com.megacreative.coding.monitoring.model;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

/**
 * Enhanced PlayerScriptMetrics with additional analysis capabilities
 */
public class PlayerScriptMetrics {
    private final UUID playerId;
    private final Map<String, ScriptMetrics> scriptMetrics = new ConcurrentHashMap<>();
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    private final AtomicInteger activeScripts = new AtomicInteger(0);
    private final AtomicInteger maxConcurrentScripts = new AtomicInteger(0);
    
    public PlayerScriptMetrics(UUID playerId) {
        this.playerId = playerId;
    }
    
    public void recordExecution(String scriptName, String actionType, long executionTime, boolean success) {
        scriptMetrics.computeIfAbsent(scriptName, ScriptMetrics::new)
                   .recordExecution(actionType, executionTime, success);
        
        totalExecutions.incrementAndGet();
        totalExecutionTime.addAndGet(executionTime);
        
        int active = activeScripts.incrementAndGet();
        maxConcurrentScripts.accumulateAndGet(active, Math::max);
        
        activeScripts.decrementAndGet();
    }
    
    public ScriptMetrics getScriptMetrics(String scriptName) {
        return scriptMetrics.get(scriptName);
    }
    
    public Collection<ScriptMetrics> getAllScriptMetrics() {
        return scriptMetrics.values();
    }
    
    public long getTotalExecutions() {
        return totalExecutions.get();
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime.get();
    }
    
    public double getAverageExecutionTime() {
        return totalExecutions.get() > 0 
            ? (double) totalExecutionTime.get() / totalExecutions.get() 
            : 0.0;
    }
    
    public int getActiveScripts() {
        return activeScripts.get();
    }
    
    public int getMaxConcurrentScripts() {
        return maxConcurrentScripts.get();
    }
    
    public UUID getPlayerId() {
        return playerId;
    }
}
