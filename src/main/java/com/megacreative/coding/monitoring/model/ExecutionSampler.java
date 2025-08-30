package com.megacreative.coding.monitoring.model;

import java.util.*;
import java.util.concurrent.*;

/**
 * Samples and analyzes execution patterns
 */
public class ExecutionSampler {
    private final Map<String, ExecutionPattern> patterns = new ConcurrentHashMap<>();
    private volatile boolean isRunning = true;
    
    /**
     * Records an execution sample
     * @param scriptName Name of the script being executed
     * @param actionType Type of action being executed
     * @param executionTime Execution time in milliseconds
     */
    public void recordExecution(String scriptName, String actionType, long executionTime) {
        String key = scriptName + "::" + actionType;
        patterns.computeIfAbsent(key, k -> new ExecutionPattern(scriptName, actionType))
               .recordExecution(executionTime);
    }
    
    /**
     * Gets all recorded execution patterns
     * @return Collection of execution patterns
     */
    public Collection<ExecutionPattern> getExecutionPatterns() {
        return new ArrayList<>(patterns.values());
    }
    
    /**
     * Starts the execution sampling service
     */
    public void start() {
        isRunning = true;
        // Start background thread for continuous sampling if needed
    }
    
    /**
     * Stops the execution sampling service
     */
    public void stop() {
        isRunning = false;
    }
    
    /**
     * Checks if the sampler is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}
