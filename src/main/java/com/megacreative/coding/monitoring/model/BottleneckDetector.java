package com.megacreative.coding.monitoring.model;

import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * Detects performance bottlenecks in script execution
 */
public class BottleneckDetector {
    private static final Logger log = Logger.getLogger(BottleneckDetector.class.getName());
    
    private final Map<String, Bottleneck> bottlenecks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private volatile boolean isRunning = false;
    private ScheduledFuture<?> detectionTask;
    
    /**
     * Detects bottlenecks in the provided script metrics
     * @param metrics Collection of script metrics to analyze
     */
    public void detectBottlenecks(Collection<ScriptMetrics> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return;
        }
        
        
        bottlenecks.clear();
        
        for (ScriptMetrics metric : metrics) {
            
            double avgTime = metric.getAverageExecutionTime();
            if (avgTime > 100) { 
                Bottleneck bottleneck = new Bottleneck(
                    metric.getScriptName(),
                    "OverallExecution",
                    "Script has high average execution time: " + String.format("%.2f", avgTime) + "ms",
                    Bottleneck.Severity.HIGH,
                    "Consider optimizing expensive operations or breaking script into smaller parts"
                );
                bottlenecks.put(metric.getScriptName() + "_avg_time", bottleneck);
            }
            
            
            double errorRate = metric.getErrorRate();
            if (errorRate > 0.1) { 
                Bottleneck bottleneck = new Bottleneck(
                    metric.getScriptName(),
                    "ErrorRate",
                    "Script has high error rate: " + String.format("%.1f", errorRate * 100) + "%",
                    Bottleneck.Severity.MEDIUM,
                    "Investigate and fix script errors to improve reliability"
                );
                bottlenecks.put(metric.getScriptName() + "_error_rate", bottleneck);
            }
            
            
            long peakTime = metric.getPeakExecutionTime();
            if (peakTime > 500) { 
                Bottleneck bottleneck = new Bottleneck(
                    metric.getScriptName(),
                    "PeakExecution",
                    "Script has very high peak execution time: " + peakTime + "ms",
                    Bottleneck.Severity.CRITICAL,
                    "This may cause noticeable lag. Optimize the most expensive operations"
                );
                bottlenecks.put(metric.getScriptName() + "_peak_time", bottleneck);
            }
        }
        
        log.info("Detected " + bottlenecks.size() + " potential bottlenecks");
    }
    
    /**
     * Gets all detected bottlenecks
     * @return Collection of detected bottlenecks
     */
    public Collection<Bottleneck> getBottlenecks() {
        return new ArrayList<>(bottlenecks.values());
    }
    
    /**
     * Starts the bottleneck detection service
     */
    public void start() {
        if (!isRunning) {
            isRunning = true;
            
            detectionTask = scheduler.scheduleAtFixedRate(this::performPeriodicDetection, 30, 30, TimeUnit.SECONDS);
            log.info("BottleneckDetector started");
        }
    }
    
    /**
     * Performs periodic bottleneck detection
     * This would be called periodically to check for new bottlenecks
     */
    private void performPeriodicDetection() {
        
        
    }
    
    /**
     * Stops the bottleneck detection service
     */
    public void stop() {
        isRunning = false;
        if (detectionTask != null) {
            detectionTask.cancel(false);
        }
        scheduler.shutdown();
        log.info("BottleneckDetector stopped");
    }
    
    /**
     * Checks if the detector is running
     * @return true if running, false otherwise
     */
    public boolean isRunning() {
        return isRunning;
    }
}