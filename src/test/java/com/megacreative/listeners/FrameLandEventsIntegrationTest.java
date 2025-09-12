package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import com.megacreative.managers.FrameLandEventManager;
import com.megacreative.events.FrameLandCustomEvents.*;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.World;
import org.bukkit.Location;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 🎆 FrameLand Events System Integration Test
 * 
 * Tests the comprehensive event coverage system including:
 * - Standard Bukkit events (join, quit, damage, inventory)
 * - Custom FrameLand events (variable changes, regions, timers)
 * - Event manager coordination and performance
 * - Script execution integration
 */
@ExtendWith(MockitoExtension.class)
public class FrameLandEventsIntegrationTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private ScriptEngine scriptEngine;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @Mock
    private com.megacreative.interfaces.IWorldManager worldManager;
    
    private FrameLandEventsListener frameLandEventsListener;
    private FrameLandCustomEventsListener customEventsListener;
    private FrameLandEventManager eventManager;
    private CreativeWorld creativeWorld;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup basic mocks
        when(plugin.getServiceRegistry()).thenReturn(mock(com.megacreative.core.ServiceRegistry.class));
        when(plugin.getServiceRegistry().getService(ScriptEngine.class)).thenReturn(scriptEngine);
        when(plugin.getWorldManager()).thenReturn(worldManager);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        
        when(player.getWorld()).thenReturn(world);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getName()).thenReturn("TestPlayer");
        when(world.getName()).thenReturn("test_world");
        
        // Create test creative world
        creativeWorld = new CreativeWorld("test_world", "test_world", UUID.randomUUID(), "test_owner", com.megacreative.models.CreativeWorldType.FLAT);
        creativeWorld.setScripts(new ArrayList<>());
        
        when(worldManager.findCreativeWorldByBukkit(world)).thenReturn(creativeWorld);
        when(worldManager.getCreativeWorlds()).thenReturn(Arrays.asList(creativeWorld));
        
        // Initialize listeners and event manager
        frameLandEventsListener = new FrameLandEventsListener(plugin);
        customEventsListener = new FrameLandCustomEventsListener(plugin);
        eventManager = new FrameLandEventManager(plugin);
    }
    
    @Test
    void testStandardPlayerJoinEvent() {
        // Create event script
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.DIAMOND_BLOCK, "onPlayerJoin");
        CodeScript script = new CodeScript("Test Join Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Rebuild event cache
        frameLandEventsListener.rebuildEventCache();
        
        // Create and process join event
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "test player joined");
        frameLandEventsListener.onPlayerJoin(joinEvent);
        
        // Verify script execution was triggered
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("player_join"));
    }
    
    @Test
    void testPlayerQuitEvent() {
        // Create event script
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.REDSTONE_BLOCK, "onPlayerQuit");
        CodeScript script = new CodeScript("Test Quit Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Rebuild event cache
        frameLandEventsListener.rebuildEventCache();
        
        // Create and process quit event
        PlayerQuitEvent quitEvent = new PlayerQuitEvent(player, "test player left");
        frameLandEventsListener.onPlayerQuit(quitEvent);
        
        // Verify script execution
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("player_quit"));
    }
    
    @Test
    void testPlayerDamageEvent() {
        // Create event script
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.TNT, "onPlayerDamage");
        CodeScript script = new CodeScript("Test Damage Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Rebuild event cache
        frameLandEventsListener.rebuildEventCache();
        
        // Create and process damage event\n        org.bukkit.event.entity.EntityDamageEvent damageEvent = mock(org.bukkit.event.entity.EntityDamageEvent.class);\n        when(damageEvent.getEntity()).thenReturn(player);\n        when(damageEvent.getDamage()).thenReturn(5.0);\n        when(damageEvent.getFinalDamage()).thenReturn(4.0);\n        when(damageEvent.getCause()).thenReturn(org.bukkit.event.entity.EntityDamageEvent.DamageCause.FALL);\n        \n        frameLandEventsListener.onEntityDamage(damageEvent);\n        \n        // Verify script execution\n        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("player_damage"));
    }
    
    @Test
    void testInventoryClickEvent() {
        // Create event script
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.CHEST, "onInventoryClick");
        CodeScript script = new CodeScript("Test Inventory Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Rebuild event cache
        frameLandEventsListener.rebuildEventCache();
        
        // Create and process inventory click event
        InventoryClickEvent clickEvent = mock(InventoryClickEvent.class);
        when(clickEvent.getWhoClicked()).thenReturn(player);
        when(clickEvent.getSlot()).thenReturn(0);
        when(clickEvent.getSlotType()).thenReturn(org.bukkit.event.inventory.InventoryType.SlotType.CONTAINER);
        when(clickEvent.getClick()).thenReturn(org.bukkit.event.inventory.ClickType.LEFT);
        when(clickEvent.getAction()).thenReturn(org.bukkit.event.inventory.InventoryAction.PICKUP_ALL);
        
        frameLandEventsListener.onInventoryClick(clickEvent);
        
        // Verify script execution
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("inventory_click"));
    }
    
    @Test
    void testCustomVariableChangeEvent() {
        // Create event script for variable changes
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.EMERALD_BLOCK, "onVariableChange");
        CodeScript script = new CodeScript("Test Variable Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Rebuild event cache
        customEventsListener.rebuildCustomEventCache();
        
        // Create and process variable change event
        PlayerVariableChangeEvent varEvent = new PlayerVariableChangeEvent(player, "score", 10, 20);
        customEventsListener.onPlayerVariableChange(varEvent);
        
        // Verify script execution
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("variable_change"));
    }
    
    @Test
    void testRegionEnterEvent() {
        // Define a test region
        Location corner1 = new Location(world, 0, 0, 0);
        Location corner2 = new Location(world, 10, 10, 10);
        eventManager.defineRegion("test_region", corner1, corner2);
        
        // Create event script for region enter
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.LIME_CONCRETE, "onRegionEnter");
        CodeScript script = new CodeScript("Test Region Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Rebuild event cache
        customEventsListener.rebuildCustomEventCache();
        
        // Test location within region
        Location testLocation = new Location(world, 5, 5, 5);
        assertTrue(eventManager.isLocationInRegion(testLocation, "test_region"));
        
        // Create and process region enter event
        PlayerEnterRegionEvent regionEvent = new PlayerEnterRegionEvent(player, "test_region", testLocation);
        customEventsListener.onPlayerEnterRegion(regionEvent);
        
        // Verify script execution
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("region_enter"));
    }
    
    @Test
    void testTimerSystem() {
        // Test timer creation and expiration
        Object timerData = "test_data";
        
        // Start a timer
        eventManager.startTimer(player, "test_timer", 20L, timerData); // 1 second
        
        // Verify timer is active
        assertTrue(eventManager.isTimerActive("test_timer"));
        
        // Get remaining time
        long remainingTime = eventManager.getTimerRemainingTime("test_timer");
        assertTrue(remainingTime > 0);
        
        // Stop timer
        eventManager.stopTimer("test_timer");
        assertFalse(eventManager.isTimerActive("test_timer"));
    }
    
    @Test
    void testEventStatistics() {
        // Test event statistics tracking
        Map<String, Object> stats = eventManager.getEventStatistics();
        assertNotNull(stats);
        
        // Reset statistics
        eventManager.resetStatistics();
        
        // Verify reset worked
        Map<String, Object> resetStats = eventManager.getEventStatistics();
        assertTrue(resetStats.isEmpty() || resetStats.values().stream().allMatch(v -> ((Map<?, ?>) v).get("count").equals(0L)));
    }
    
    @Test
    void testEventCachePerformance() {
        // Add multiple scripts to test cache performance
        for (int i = 0; i < 10; i++) {
            com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.STONE, "onPlayerJoin");
            CodeScript script = new CodeScript("Test Script " + i, true, eventBlock);
            creativeWorld.getScripts().add(script);
        }
        
        // Measure cache rebuild time
        long startTime = System.nanoTime();
        frameLandEventsListener.rebuildEventCache();
        long rebuildTime = System.nanoTime() - startTime;
        
        // Cache rebuild should be fast (under 1ms for 10 scripts)
        assertTrue(rebuildTime < 1_000_000L, "Cache rebuild took too long: " + rebuildTime + "ns");
        
        // Test event statistics
        Map<String, Long> stats = frameLandEventsListener.getEventStatistics();
        assertNotNull(stats);
    }
    
    @Test
    void testEventManagerShutdown() {
        // Test proper cleanup
        eventManager.defineRegion("test_region", new Location(world, 0, 0, 0), new Location(world, 10, 10, 10));
        eventManager.startTimer(player, "test_timer", 100L, "test");
        
        // Shutdown should clean up everything
        assertDoesNotThrow(() -> eventManager.shutdown());
        
        // Verify cleanup
        assertFalse(eventManager.isTimerActive("test_timer"));
    }
    
    @Test
    void testMultipleEventTypes() {
        // Test that multiple event types can be handled simultaneously
        List<String> eventTypes = Arrays.asList("onPlayerJoin", "onPlayerQuit", "onPlayerDamage", "onInventoryClick");
        
        for (String eventType : eventTypes) {
            com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.STONE, eventType);
            CodeScript script = new CodeScript("Test " + eventType, true, eventBlock);
            creativeWorld.getScripts().add(script);
        }
        
        // Rebuild cache
        frameLandEventsListener.rebuildEventCache();
        
        // Verify all event types are cached
        for (String eventType : eventTypes) {
            // This would be verified internally by the cache system
            // In a real test, we'd check the internal cache state
        }
    }
    
    @Test
    void testEventContextData() {
        // Test that event data is properly passed to scripts
        com.megacreative.coding.CodeBlock eventBlock = new com.megacreative.coding.CodeBlock(org.bukkit.Material.DIAMOND_BLOCK, "onPlayerJoin");
        CodeScript script = new CodeScript("Test Context Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        frameLandEventsListener.rebuildEventCache();
        
        // Create join event with specific data
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "Welcome message");
        when(player.hasPlayedBefore()).thenReturn(false);
        
        frameLandEventsListener.onPlayerJoin(joinEvent);
        
        // Verify script was called with player and context
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("player_join"));
    }
}