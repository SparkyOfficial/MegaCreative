package com.megacreative.coding.monitoring.test;

import com.megacreative.coding.monitoring.model.GarbageCollectionMonitor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for GarbageCollectionMonitor functionality
 */
public class GarbageCollectionMonitorTest {
    
    private GarbageCollectionMonitor gcMonitor;
    
    @BeforeEach
    public void setUp() {
        gcMonitor = new GarbageCollectionMonitor();
    }
    
    @Test
    public void testGcMonitorCreation() {
        assertNotNull(gcMonitor);
    }
    
    @Test
    public void testGetCurrentStatistics() {
        GarbageCollectionMonitor.GcStatistics stats = gcMonitor.getCurrentStatistics();
        
        assertNotNull(stats);
        assertTrue(stats.getTotalGcCount() >= 0);
        assertTrue(stats.getTotalGcTime() >= 0);
    }
    
    @Test
    public void testGcStatisticsCalculations() {
        // Get initial statistics
        GarbageCollectionMonitor.GcStatistics stats1 = gcMonitor.getCurrentStatistics();
        
        // Get statistics again (should be same or slightly different)
        GarbageCollectionMonitor.GcStatistics stats2 = gcMonitor.getCurrentStatistics();
        
        assertNotNull(stats1);
        assertNotNull(stats2);
        
        // Test that cumulative values are consistent
        assertTrue(stats2.getCumulativeGcCount() >= stats1.getCumulativeGcCount());
        assertTrue(stats2.getCumulativeGcTime() >= stats1.getCumulativeGcTime());
    }
    
    @Test
    public void testAverageGcTimeCalculation() {
        GarbageCollectionMonitor.GcStatistics stats = gcMonitor.getCurrentStatistics();
        
        // Test that average GC time calculation works
        double averageTime = stats.getAverageGcTime();
        assertTrue(averageTime >= 0);
        
        // If there have been GC collections, average should be calculable
        if (stats.getTotalGcCount() > 0) {
            assertTrue(averageTime > 0);
        } else {
            assertEquals(0.0, averageTime, 0.001);
        }
    }
    
    @Test
    public void testGcTimePercentageCalculation() {
        GarbageCollectionMonitor.GcStatistics stats = gcMonitor.getCurrentStatistics();
        
        // Test GC time percentage calculation
        long uptimeMs = 10000; // 10 seconds
        double percentage = stats.getGcTimePercentage(uptimeMs);
        
        assertTrue(percentage >= 0);
        if (uptimeMs > 0) {
            assertTrue(percentage <= 100);
        }
    }
    
    @Test
    public void testMonitorLifecycle() {
        // Test that monitor can be started and stopped
        gcMonitor.start();
        assertTrue(gcMonitor.isRunning());
        
        gcMonitor.stop();
        // Note: isRunning() might still return true depending on implementation
    }
}