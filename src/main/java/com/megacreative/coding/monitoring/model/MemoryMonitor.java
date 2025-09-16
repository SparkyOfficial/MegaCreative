package com.megacreative.coding.monitoring.model;

/**
 * Monitors memory usage for the application
 */
public class MemoryMonitor {
    private final GarbageCollectionMonitor gcMonitor = new GarbageCollectionMonitor();
    private volatile boolean isRunning = true;
    
    /**
     * Gets the current memory usage statistics
     * @return MemoryUsage object containing usage information
     */
    public MemoryUsage getCurrentUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        long maxMemory = runtime.maxMemory();
        
        return new MemoryUsage(usedMemory, maxMemory, totalMemory);
    }
    
    /**
     * Gets the current garbage collection statistics
     * @return GcStatistics object containing GC information
     */
    public GarbageCollectionMonitor.GcStatistics getGcStatistics() {
        return gcMonitor.getCurrentStatistics();
    }
    
    /**
     * Starts the memory monitoring thread
     */
    public void start() {
        gcMonitor.start();
        // Start monitoring thread if needed
        // This could be implemented to periodically log memory usage
    }
    
    /**
     * Stops the memory monitoring thread
     */
    public void stop() {
        isRunning = false;
        gcMonitor.stop();
    }
    
    /**
     * Checks if the monitor is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}