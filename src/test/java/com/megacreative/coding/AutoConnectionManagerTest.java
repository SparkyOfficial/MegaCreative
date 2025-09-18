package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.BlockConfigService;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Server;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AutoConnectionManagerTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Mock
    private Server server;

    private AutoConnectionManager autoConnectionManager;
    private Location testLocation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        autoConnectionManager = new AutoConnectionManager(plugin, blockConfigService);
        
        // Set up test location
        testLocation = new Location(world, 0, 64, 0);
        
        // Set up world
        when(world.getName()).thenReturn("dev_world");
        
        // Set up player
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(player.getWorld()).thenReturn(world);
        
        // Set up plugin
        when(plugin.getServer()).thenReturn(server);
        when(server.getWorlds()).thenReturn(java.util.Collections.singletonList(world));
    }

    @Test
    void testAddCodeBlock() {
        CodeBlock codeBlock = new CodeBlock(Material.STONE, "test_action");
        autoConnectionManager.addCodeBlock(testLocation, codeBlock);
        
        // Verify the block was added
        // Note: We can't directly access the private field, but we can test indirectly
        // by checking if methods that use it work correctly
    }

    @Test
    void testDisconnectBlock() {
        CodeBlock codeBlock = new CodeBlock(Material.STONE, "test_action");
        autoConnectionManager.addCodeBlock(testLocation, codeBlock);
        
        // Test disconnecting the block
        autoConnectionManager.disconnectBlock(codeBlock, testLocation);
        
        // Verify the block was removed from tracking
        // Note: We can't directly access the private field, but we can test indirectly
    }

    @Test
    void testGetPreviousLocationInLine() {
        Location location = new Location(world, 5, 64, 10);
        Location previous = autoConnectionManager.getPreviousLocationInLine(location);
        
        assertNotNull(previous);
        assertEquals(4, previous.getBlockX());
        assertEquals(64, previous.getBlockY());
        assertEquals(10, previous.getBlockZ());
    }

    @Test
    void testGetNextLocationInLine() {
        Location location = new Location(world, 5, 64, 10);
        Location next = autoConnectionManager.getNextLocationInLine(location);
        
        assertNotNull(next);
        assertEquals(6, next.getBlockX());
        assertEquals(64, next.getBlockY());
        assertEquals(10, next.getBlockZ());
    }

    @Test
    void testFindBlockOwner() {
        // Test finding block owner
        Player owner = autoConnectionManager.findBlockOwner(testLocation);
        // This will return null in test environment since there are no actual players
        assertNull(owner);
    }

    @Test
    void testIsEventBlock() {
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Mock the block config service to return an event block config
        BlockConfigService.BlockConfig config = mock(BlockConfigService.BlockConfig.class);
        when(config.getType()).thenReturn("EVENT");
        when(blockConfigService.getBlockConfig("onJoin")).thenReturn(config);
        
        assertTrue(autoConnectionManager.isEventBlock(eventBlock));
    }

    @Test
    void testGetConnectionStats() {
        String stats = autoConnectionManager.getConnectionStats();
        assertNotNull(stats);
        assertTrue(stats.contains("Blocks: 0"));
    }

    @Test
    void testGetCodeBlockMaterials() {
        // Test getting code block materials
        // This will return an empty set since we haven't configured any materials in the mock
        assertNotNull(autoConnectionManager.getCodeBlockMaterials());
    }
}