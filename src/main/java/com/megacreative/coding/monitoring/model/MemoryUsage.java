package com.megacreative.coding.monitoring.model;

/**
 * Represents memory usage statistics
 */
public class MemoryUsage {
    private final long usedMemory;
    private final long maxMemory;
    private final long totalMemory;
    
    public MemoryUsage(long usedMemory, long maxMemory, long totalMemory) {
        this.usedMemory = usedMemory;
        this.maxMemory = maxMemory;
        this.totalMemory = totalMemory;
    }
    
    public long getUsedMemory() {
        return usedMemory;
    }
    
    public long getMaxMemory() {
        return maxMemory;
    }
    
    public long getTotalMemory() {
        return totalMemory;
    }
    
    public double getUsedMemoryMB() {
        return usedMemory / (1024.0 * 1024.0);
    }
    
    public double getMaxMemoryMB() {
        return maxMemory / (1024.0 * 1024.0);
    }
    
    public double getTotalMemoryMB() {
        return totalMemory / (1024.0 * 1024.0);
    }
    
    public double getMemoryUsagePercentage() {
        return maxMemory > 0 ? (usedMemory * 100.0) / maxMemory : 0.0;
    }
}
