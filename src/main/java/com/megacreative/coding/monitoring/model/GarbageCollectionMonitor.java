package com.megacreative.coding.monitoring.model;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Logger;

/**
 * Monitors garbage collection statistics for the application
 */
public class GarbageCollectionMonitor {
    private static final Logger log = Logger.getLogger(GarbageCollectionMonitor.class.getName());
    
    private final List<GarbageCollectorMXBean> gcBeans;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean isRunning = false;
    private volatile long lastGcCount = 0;
    private volatile long lastGcTime = 0;
    private final AtomicLong totalGcCount = new AtomicLong(0);
    private final AtomicLong totalGcTime = new AtomicLong(0);
    
    public GarbageCollectionMonitor() {
        this.gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
    }
    
    /**
     * Gets the current garbage collection statistics
     * @return GcStatistics object containing GC information
     */
    public GcStatistics getCurrentStatistics() {
        long gcCount = 0;
        long gcTime = 0;
        
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            gcCount += gcBean.getCollectionCount();
            gcTime += gcBean.getCollectionTime();
        }
        
        
        long gcCountDelta = gcCount - lastGcCount;
        long gcTimeDelta = gcTime - lastGcTime;
        
        
        lastGcCount = gcCount;
        lastGcTime = gcTime;
        
        
        totalGcCount.addAndGet(gcCountDelta);
        totalGcTime.addAndGet(gcTimeDelta);
        
        return new GcStatistics(gcCount, gcTime, gcCountDelta, gcTimeDelta, totalGcCount.get(), totalGcTime.get());
    }
    
    /**
     * Starts the garbage collection monitoring thread
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            
            getCurrentStatistics();
            log.fine("GarbageCollectionMonitor started");
        }
    }
    
    /**
     * Stops the garbage collection monitoring thread
     */
    public void stop() {
        isRunning = false;
        scheduler.shutdown();
        log.fine("GarbageCollectionMonitor stopped");
    }
    
    /**
     * Checks if the monitor is currently running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Represents garbage collection statistics
     */
    public static class GcStatistics {
        private final long totalGcCount;
        private final long totalGcTime;
        private final long gcCountDelta;
        private final long gcTimeDelta;
        private final long cumulativeGcCount;
        private final long cumulativeGcTime;
        
        public GcStatistics(long totalGcCount, long totalGcTime, 
                          long gcCountDelta, long gcTimeDelta,
                          long cumulativeGcCount, long cumulativeGcTime) {
            this.totalGcCount = totalGcCount;
            this.totalGcTime = totalGcTime;
            this.gcCountDelta = gcCountDelta;
            this.gcTimeDelta = gcTimeDelta;
            this.cumulativeGcCount = cumulativeGcCount;
            this.cumulativeGcTime = cumulativeGcTime;
        }
        
        public long getTotalGcCount() {
            return totalGcCount;
        }
        
        public long getTotalGcTime() {
            return totalGcTime;
        }
        
        public long getGcCountDelta() {
            return gcCountDelta;
        }
        
        public long getGcTimeDelta() {
            return gcTimeDelta;
        }
        
        public long getCumulativeGcCount() {
            return cumulativeGcCount;
        }
        
        public long getCumulativeGcTime() {
            return cumulativeGcTime;
        }
        
        public double getAverageGcTime() {
            return totalGcCount > 0 ? (double) totalGcTime / totalGcCount : 0.0;
        }
        
        /**
         * Gets GC time as percentage of total application time
         * Note: This is approximate and requires knowing the total application uptime
         */
        public double getGcTimePercentage(long applicationUptimeMs) {
            return applicationUptimeMs > 0 ? (totalGcTime * 100.0) / applicationUptimeMs : 0.0;
        }
    }
}