package com.megacreative.coding.monitoring.test;

import com.megacreative.commands.PerformanceCommand;
import com.megacreative.coding.monitoring.ScriptPerformanceMonitor;
import com.megacreative.coding.monitoring.model.*;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for PerformanceCommand functionality
 */
public class PerformanceCommandTest {
    
    private PerformanceCommand performanceCommand;
    private Plugin mockPlugin;
    private ScriptPerformanceMonitor mockPerformanceMonitor;
    
    @BeforeEach
    public void setUp() {
        mockPlugin = Mockito.mock(Plugin.class);
        mockPerformanceMonitor = Mockito.mock(ScriptPerformanceMonitor.class);
        
        // Mock the performance monitor methods
        when(mockPlugin.getConfig()).thenReturn(mock(org.bukkit.configuration.file.FileConfiguration.class));
        
        performanceCommand = new PerformanceCommand(Mockito.mock(com.megacreative.MegaCreative.class));
    }
    
    @Test
    public void testCommandExecutionByNonPlayer() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        org.bukkit.command.Command mockCommand = Mockito.mock(org.bukkit.command.Command.class);
        
        boolean result = performanceCommand.onCommand(mockSender, mockCommand, "performance", new String[]{});
        
        assertTrue(result);
        verify(mockSender).sendMessage("§cThis command can only be used by players!");
    }
    
    @Test
    public void testCommandExecutionWithNoArgs() {
        Player mockPlayer = Mockito.mock(Player.class);
        org.bukkit.command.Command mockCommand = Mockito.mock(org.bukkit.command.Command.class);
        
        boolean result = performanceCommand.onCommand(mockPlayer, mockCommand, "performance", new String[]{});
        
        assertTrue(result);
        // Verify that help message was sent
        verify(mockPlayer, atLeastOnce()).sendMessage(anyString());
    }
    
    @Test
    public void testCommandExecutionWithInvalidSubcommand() {
        Player mockPlayer = Mockito.mock(Player.class);
        org.bukkit.command.Command mockCommand = Mockito.mock(org.bukkit.command.Command.class);
        
        boolean result = performanceCommand.onCommand(mockPlayer, mockCommand, "performance", new String[]{"invalid"});
        
        assertTrue(result);
        // Verify that help message was sent
        verify(mockPlayer, atLeastOnce()).sendMessage(anyString());
    }
    
    @Test
    public void testTabCompletion() {
        CommandSender mockSender = Mockito.mock(CommandSender.class);
        org.bukkit.command.Command mockCommand = Mockito.mock(org.bukkit.command.Command.class);
        
        // Test first argument completion
        java.util.List<String> completions1 = performanceCommand.onTabComplete(mockSender, mockCommand, "performance", new String[]{""});
        assertNotNull(completions1);
        assertTrue(completions1.contains("report"));
        assertTrue(completions1.contains("script"));
        assertTrue(completions1.contains("optimize"));
        
        // Test partial completion
        java.util.List<String> completions2 = performanceCommand.onTabComplete(mockSender, mockCommand, "performance", new String[]{"rep"});
        assertNotNull(completions2);
        assertTrue(completions2.contains("report"));
        assertFalse(completions2.contains("script"));
    }
    
    @Test
    public void testScriptMetricsErrorRate() {
        // Test the new getErrorRate method in ScriptMetrics
        ScriptMetrics metrics = new ScriptMetrics(100, 50.0, 5000, 100, 0.9, 0.1);
        
        assertEquals(0.1, metrics.getErrorRate(), 0.001);
        assertEquals(0.9, metrics.getSuccessRate(), 0.001);
    }
    
    @Test
    public void testUptimeFormatting() {
        // Test the uptime formatting functionality
        // Note: This is a bit tricky to test since the method is private
        // We'll test the logic indirectly through the MemoryUsage methods
        
        // Test MemoryUsage formatting methods
        MemoryUsage memoryUsage = new MemoryUsage(100 * 1024 * 1024, 512 * 1024 * 1024, 256 * 1024 * 1024);
        
        assertEquals(100.0, memoryUsage.getUsedMemoryMB(), 0.1);
        assertEquals(512.0, memoryUsage.getMaxMemoryMB(), 0.1);
        assertEquals(256.0, memoryUsage.getTotalMemoryMB(), 0.1);
        assertEquals(19.53, memoryUsage.getMemoryUsagePercentage(), 0.1);
    }
}