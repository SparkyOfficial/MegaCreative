package com.megacreative.coding.monitoring.model;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Monitors memory usage for the application
 */
public class MemoryMonitor {
    private final GarbageCollectionMonitor gcMonitor = new GarbageCollectionMonitor();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean isRunning = false;
    
    public MemoryMonitor() {
        
    }
    
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
        if (!isRunning) {
            isRunning = true;
            gcMonitor.start();
            
            scheduler.scheduleAtFixedRate(this::logMemoryUsage, 0, 30, TimeUnit.SECONDS);
        }
    }
    
    private void logMemoryUsage() {
        
    }
    
    /**
     * Stops the memory monitoring thread
     */
    public void stop() {
        isRunning = false;
        gcMonitor.stop();
        scheduler.shutdown();
    }
    
    /**
     * Checks if the monitor is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}