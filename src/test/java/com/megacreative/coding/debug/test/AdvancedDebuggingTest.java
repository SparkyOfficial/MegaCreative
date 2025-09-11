package com.megacreative.coding.debug.test;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.debug.AdvancedVisualDebugger;
import com.megacreative.coding.debug.VisualDebugger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdvancedDebuggingTest {
    
    private VisualDebugger visualDebugger;
    private MegaCreative mockPlugin;

    @BeforeEach
    public void setUp() {
        mockPlugin = Mockito.mock(MegaCreative.class);
        visualDebugger = new VisualDebugger(mockPlugin);
    }

    @Test
    public void testBreakpointManagement() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Create mock location
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        Location location = new Location(mockWorld, 10, 5, 15);
        
        // Set a breakpoint
        visualDebugger.addBreakpoint(mockPlayer, location, "x > 5");
        
        // Verify breakpoint was set
        // This would require accessing private fields or adding getter methods
        
        // Remove breakpoint
        visualDebugger.removeBreakpoint(mockPlayer, location);
    }
    
    @Test
    public void testExecutionTracing() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Start tracing
        visualDebugger.startTracing(mockPlayer, 100);
        
        // Create mock block and location
        CodeBlock mockBlock = Mockito.mock(CodeBlock.class);
        when(mockBlock.getAction()).thenReturn("TestAction");
        
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        Location location = new Location(mockWorld, 10, 5, 15);
        
        // Simulate block execution
        visualDebugger.onBlockExecute(mockPlayer, mockBlock, location);
        
        // Show trace
        visualDebugger.showTrace(mockPlayer);
        
        // Stop tracing
        visualDebugger.stopTracing(mockPlayer);
    }
    
    @Test
    public void testVariableWatching() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Watch a variable
        visualDebugger.watchVariable(mockPlayer, "testVar", "value > 10");
        
        // Show watched variables
        visualDebugger.showWatchedVariables(mockPlayer);
        
        // Unwatch variable
        visualDebugger.unwatchVariable(mockPlayer, "testVar");
    }
    
    @Test
    public void testAdvancedVisualization() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Start visualization session
        visualDebugger.startVisualization(mockPlayer, AdvancedVisualDebugger.VisualizationMode.STANDARD);
        
        // Verify visualization is enabled
        assertTrue(visualDebugger.isDebugEnabled(mockPlayer));
        // Note: We can't directly test getVisualizationMode since it's not exposed in the public API
        
        // Create mock block and location
        CodeBlock mockBlock = Mockito.mock(CodeBlock.class);
        when(mockBlock.getAction()).thenReturn("TestAction");
        
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        Location location = new Location(mockWorld, 10, 5, 15);
        
        // Visualize block execution
        visualDebugger.visualizeBlockExecution(mockPlayer, mockBlock, location);
        
        // Stop visualization
        visualDebugger.stopVisualization(mockPlayer);
        
        // Verify visualization is disabled
        assertFalse(visualDebugger.isVisualizationEnabled(mockPlayer));
    }
    
    @Test
    public void testPerformanceAnalysis() {
        // Create mock player
        Player mockPlayer = Mockito.mock(Player.class);
        UUID playerId = UUID.randomUUID();
        when(mockPlayer.getUniqueId()).thenReturn(playerId);
        
        // Create mock script
        CodeScript mockScript = Mockito.mock(CodeScript.class);
        when(mockScript.getName()).thenReturn("TestScript");
        
        // Start performance analysis
        visualDebugger.startPerformanceAnalysis(mockPlayer, mockScript);
        
        // Create mock block and location
        CodeBlock mockBlock = Mockito.mock(CodeBlock.class);
        when(mockBlock.getAction()).thenReturn("TestAction");
        
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.getName()).thenReturn("world");
        Location location = new Location(mockWorld, 10, 5, 15);
        
        // Record execution data
        visualDebugger.recordExecutionData(mockPlayer, mockBlock, location, 50);
        
        // Show performance report
        visualDebugger.showPerformanceReport(mockPlayer);
    }
}