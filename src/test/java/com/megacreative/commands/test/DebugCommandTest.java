package com.megacreative.commands.test;

import com.megacreative.MegaCreative;
import com.megacreative.commands.DebugCommand;
import com.megacreative.coding.debug.VisualDebugger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.Mockito.*;

class DebugCommandTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private Command command;
    
    @Mock
    private VisualDebugger visualDebugger;
    
    @Mock
    private World world;
    
    private DebugCommand debugCommand;
    private UUID playerUUID;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        debugCommand = new DebugCommand(plugin);
        playerUUID = UUID.randomUUID();
        
        when(plugin.getScriptDebugger()).thenReturn(visualDebugger);
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("test_world");
    }
    
    @Test
    void testToggleDebug() {
        // Test toggling debug on
        when(visualDebugger.isDebugEnabled(player)).thenReturn(false);
        debugCommand.onCommand(player, command, "debug", new String[0]);
        verify(visualDebugger).toggleDebug(player);
    }
    
    @Test
    void testEnableDebug() {
        // Test enabling debug
        when(visualDebugger.isDebugEnabled(player)).thenReturn(false);
        debugCommand.onCommand(player, command, "debug", new String[]{"on"});
        verify(visualDebugger).toggleDebug(player);
    }
    
    @Test
    void testDisableDebug() {
        // Test disabling debug
        when(visualDebugger.isDebugEnabled(player)).thenReturn(true);
        debugCommand.onCommand(player, command, "debug", new String[]{"off"});
        verify(visualDebugger).toggleDebug(player);
    }
    
    @Test
    void testShowStats() {
        // Test showing debug stats
        debugCommand.onCommand(player, command, "debug", new String[]{"stats"});
        verify(visualDebugger).showDebugStats(player);
    }
    
    @Test
    void testShowStatus() {
        // Test showing debug status
        debugCommand.onCommand(player, command, "debug", new String[]{"status"});
        verify(visualDebugger).isDebugEnabled(player);
    }
    
    @Test
    void testSetBreakpoint() {
        // Test setting breakpoint at current location
        debugCommand.onCommand(player, command, "debug", new String[]{"breakpoint", "set"});
        verify(visualDebugger).setBreakpoint(player, player.getLocation(), null);
    }
    
    @Test
    void testSetBreakpointWithCoordinates() {
        // Test setting breakpoint at specific coordinates
        String[] args = {"breakpoint", "set", "10", "64", "20"};
        debugCommand.onCommand(player, command, "debug", args);
        verify(visualDebugger).setBreakpoint(eq(player), any(Location.class), isNull());
    }
    
    @Test
    void testListBreakpoints() {
        // Test listing breakpoints
        debugCommand.onCommand(player, command, "debug", new String[]{"breakpoint", "list"});
        verify(visualDebugger).listBreakpoints(player);
    }
    
    @Test
    void testWatchVariable() {
        // Test watching a variable
        String[] args = {"watch", "add", "player_name"};
        debugCommand.onCommand(player, command, "debug", args);
        verify(visualDebugger).watchVariable(player, "player_name", null);
    }
    
    @Test
    void testListWatchedVariables() {
        // Test listing watched variables
        debugCommand.onCommand(player, command, "debug", new String[]{"watch", "list"});
        verify(visualDebugger).showWatchedVariables(player);
    }
    
    @Test
    void testStartTracing() {
        // Test starting execution tracing
        debugCommand.onCommand(player, command, "debug", new String[]{"trace", "start"});
        verify(visualDebugger).startTracing(player, 100); // Default max steps
    }
    
    @Test
    void testShowTrace() {
        // Test showing execution trace
        debugCommand.onCommand(player, command, "debug", new String[]{"trace", "show"});
        verify(visualDebugger).showTrace(player);
    }
    
    @Test
    void testShowPerformanceReport() {
        // Test showing performance report
        debugCommand.onCommand(player, command, "debug", new String[]{"performance", "report"});
        verify(visualDebugger).showPerformanceReport(player);
    }
    
    @Test
    void testShowHelp() {
        // Test showing help
        debugCommand.onCommand(player, command, "debug", new String[]{"help"});
        verify(player).sendMessage(contains("=== Отладка скриптов ==="));
    }
}