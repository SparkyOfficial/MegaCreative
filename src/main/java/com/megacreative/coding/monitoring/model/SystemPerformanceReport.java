package com.megacreative.coding.monitoring.model;

import java.util.*;

/**
 * System performance report with aggregated metrics
 */
public class SystemPerformanceReport {
    private final long totalExecutions;
    private final long totalExecutionTime;
    private final int activePlayerCount;
    private final int scriptProfilesCount;
    private final MemoryUsage memoryUsage;
    private final GarbageCollectionMonitor.GcStatistics gcStatistics;
    private final Collection<Bottleneck> bottlenecks;
    private final int uniqueActionTypes;
    private final long uptimeMs;
    
    public SystemPerformanceReport(long totalExecutions, long totalExecutionTime, 
                                 int activePlayerCount, int scriptProfilesCount,
                                 MemoryUsage memoryUsage, 
                                 GarbageCollectionMonitor.GcStatistics gcStatistics,
                                 Collection<Bottleneck> bottlenecks,
                                 long uptimeMs) {
        this.totalExecutions = totalExecutions;
        this.totalExecutionTime = totalExecutionTime;
        this.activePlayerCount = activePlayerCount;
        this.scriptProfilesCount = scriptProfilesCount;
        this.memoryUsage = memoryUsage;
        this.gcStatistics = gcStatistics;
        this.bottlenecks = bottlenecks != null ? new ArrayList<>(bottlenecks) : Collections.emptyList();
        this.uniqueActionTypes = 0; // This would be calculated from actual data
        this.uptimeMs = uptimeMs;
    }
    
    public long getTotalExecutions() {
        return totalExecutions;
    }
    
    public long getTotalExecutionTime() {
        return totalExecutionTime;
    }
    
    public int getActivePlayerCount() {
        return activePlayerCount;
    }
    
    public int getScriptProfilesCount() {
        return scriptProfilesCount;
    }
    
    public MemoryUsage getMemoryUsage() {
        return memoryUsage;
    }
    
    public GarbageCollectionMonitor.GcStatistics getGcStatistics() {
        return gcStatistics;
    }
    
    public Collection<Bottleneck> getBottlenecks() {
        return Collections.unmodifiableCollection(bottlenecks);
    }
    
    public int getUniqueActionTypes() {
        return uniqueActionTypes;
    }
    
    public double getAverageExecutionTime() {
        return totalExecutions > 0 ? (double) totalExecutionTime / totalExecutions : 0.0;
    }
    
    public long getUptimeMs() {
        return uptimeMs;
    }
}