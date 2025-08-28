package com.megacreative.coding.debug.test;

import com.megacreative.MegaCreative;
import com.megacreative.coding.debug.VisualDebugger;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.mockito.Mockito.*;

class EnhancedDebuggingTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    private VisualDebugger debugger;
    private UUID playerUUID;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        debugger = new VisualDebugger(plugin);
        playerUUID = UUID.randomUUID();
        
        when(player.getUniqueId()).thenReturn(playerUUID);
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("test_world");
    }
    
    @Test
    void testBreakpointManagement() {
        // Test setting a breakpoint
        Location location = new Location(world, 10, 64, 20);
        debugger.setBreakpoint(player, location, "player.health < 10");
        
        // Verify breakpoint was set
        debugger.listBreakpoints(player);
        verify(player).sendMessage("§a§lActive Breakpoints:");
        verify(player).sendMessage(contains("§7- §f§7[10, 64, 20] §7[player.health < 10]"));
        
        // Test removing a breakpoint
        debugger.removeBreakpoint(player, location);
        verify(player).sendMessage("§a✓ Breakpoint removed at §7[10, 64, 20]");
    }
    
    @Test
    void testVariableWatching() {
        // Test adding a variable watcher
        debugger.watchVariable(player, "player_name", "player.getName()");
        
        // Verify variable watcher was added
        debugger.showWatchedVariables(player);
        verify(player).sendMessage("§a§lWatched Variables:");
        verify(player).sendMessage("§7- §fplayer_name §7= §bplayer.getName()");
        
        // Test removing a variable watcher
        debugger.unwatchVariable(player, "player_name");
        verify(player).sendMessage("§a✓ Stopped watching variable: player_name");
    }
    
    @Test
    void testExecutionTracing() {
        // First enable debug mode so messages will be sent
        debugger.toggleDebug(player);
        
        // Test starting execution tracing
        debugger.startTracing(player, 100);
        verify(player).sendMessage("§a✓ Execution tracing started (max 100 steps)");
        
        // Test stopping execution tracing
        debugger.stopTracing(player);
        verify(player).sendMessage("§c✖ Execution tracing stopped");
        verify(player).sendMessage("§7Traced 0 execution steps");
    }
    
    @Test
    void testDebugModeToggle() {
        // Test enabling debug mode
        boolean enabled = debugger.toggleDebug(player);
        verify(player).sendMessage("§a✓ Visual debugger started: Debug Session");
        assert enabled;
        
        // Test debug status
        boolean isEnabled = debugger.isDebugEnabled(player);
        assert isEnabled;
        
        // Test showing debug stats
        debugger.showDebugStats(player);
        verify(player).sendMessage("§6=== Debug Statistics ===");
        
        // Test disabling debug mode
        boolean disabled = debugger.toggleDebug(player);
        verify(player).sendMessage("§c✖ Visual debugger stopped");
        assert !disabled;
    }
    
    @Test
    void testPerformanceReporting() {
        // First enable debug mode
        debugger.toggleDebug(player);
        
        // Test showing performance report (should show no data message)
        debugger.showPerformanceReport(player);
        verify(player).sendMessage("§cNo performance analysis data available");
    }
}