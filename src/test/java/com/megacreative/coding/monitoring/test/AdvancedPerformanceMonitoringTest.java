package com.megacreative.coding.monitoring.test;

import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import com.megacreative.coding.monitoring.AdvancedScriptOptimizer;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for advanced performance monitoring features
 */
public class AdvancedPerformanceMonitoringTest {
    
    private ScriptPerformanceMonitor performanceMonitor;
    private Plugin mockPlugin;
    
    @BeforeEach
    public void setUp() {
        mockPlugin = Mockito.mock(Plugin.class);
        when(mockPlugin.getConfig()).thenReturn(mock(org.bukkit.configuration.file.FileConfiguration.class));
        performanceMonitor = new ScriptPerformanceMonitor(mockPlugin);
    }
    
    @Test
    public void testExecutionTracking() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Start tracking
        ScriptPerformanceMonitor.ExecutionTracker tracker = performanceMonitor.startTracking(
            mockPlayer, "TestScript", "sendMessage");
        
        // Simulate some work
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // End tracking
        tracker.close();
        
        // Verify metrics were recorded
        ScriptPerformanceMonitor.PlayerScriptMetrics metrics = performanceMonitor.getPlayerMetrics(playerId);
        assertNotNull(metrics);
        assertEquals(1, metrics.getTotalExecutions());
    }
    
    @Test
    public void testScriptProfiling() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Execute multiple times to build profile
        for (int i = 0; i < 5; i++) {
            ScriptPerformanceMonitor.ExecutionTracker tracker = performanceMonitor.startTracking(
                mockPlayer, "ProfileTestScript", "setVariable");
            
            // Simulate work
            try {
                Thread.sleep(5);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            tracker.close();
        }
        
        // Get script profile
        ScriptPerformanceMonitor.ScriptPerformanceProfile profile = 
            performanceMonitor.getScriptProfile("ProfileTestScript");
        
        assertNotNull(profile);
        assertEquals("ProfileTestScript", profile.getScriptName());
        assertEquals(5, profile.getTotalExecutions());
        assertTrue(profile.getAverageExecutionTime() > 0);
    }
    
    @Test
    public void testPerformanceReportGeneration() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Execute some actions
        ScriptPerformanceMonitor.ExecutionTracker tracker1 = performanceMonitor.startTracking(
            mockPlayer, "ReportTestScript", "sendMessage");
        tracker1.close();
        
        ScriptPerformanceMonitor.ExecutionTracker tracker2 = performanceMonitor.startTracking(
            mockPlayer, "ReportTestScript", "setVariable");
        tracker2.close();
        
        // Generate report
        ScriptPerformanceMonitor.SystemPerformanceReport report = performanceMonitor.getSystemReport();
        
        assertNotNull(report);
        assertEquals(2, report.getTotalExecutions());
        assertTrue(report.getAverageExecutionTime() >= 0);
    }
    
    @Test
    public void testAdvancedScriptOptimizer() {
        // Create optimizer
        AdvancedScriptOptimizer optimizer = new AdvancedScriptOptimizer(performanceMonitor);
        
        // Create mock script
        CodeScript mockScript = Mockito.mock(CodeScript.class);
        when(mockScript.getName()).thenReturn("TestScript");
        when(mockScript.getBlocks()).thenReturn(java.util.Collections.emptyList());
        
        // Analyze script
        AdvancedScriptOptimizer.ScriptOptimizationReport report = optimizer.analyzeScript(mockScript);
        
        assertNotNull(report);
        assertEquals("TestScript", report.getScriptName());
        assertNotNull(report.getSuggestions());
    }
    
    @Test
    public void testBottleneckDetection() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Execute a slow action to trigger bottleneck detection
        ScriptPerformanceMonitor.ExecutionTracker tracker = performanceMonitor.startTracking(
            mockPlayer, "BottleneckTestScript", "expensiveAction");
        
        // Simulate slow execution
        try {
            Thread.sleep(60); // Above the 50ms threshold
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        tracker.close();
        
        // Get system report to check for bottlenecks
        ScriptPerformanceMonitor.AdvancedSystemPerformanceReport report = 
            performanceMonitor.getAdvancedSystemReport();
        
        assertNotNull(report);
        assertNotNull(report.getBottlenecks());
    }
}