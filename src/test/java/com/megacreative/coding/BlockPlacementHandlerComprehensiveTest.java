package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockPlacementHandlerComprehensiveTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private Player player;

    @Mock
    private Block block;

    @Mock
    private BlockState blockState;

    @Mock
    private World world;

    @Mock
    private Location location;

    @Mock
    private ItemStack itemStack;

    @Mock
    private PlayerInventory playerInventory;

    private BlockPlacementHandler blockPlacementHandler;

    @BeforeEach
    void setUp() {
        // Set up mocks
        lenient().when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(player.getName()).thenReturn("TestPlayer");
        lenient().when(player.getInventory()).thenReturn(playerInventory);
        lenient().when(block.getType()).thenReturn(Material.COBBLESTONE);
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(block.getState()).thenReturn(blockState);
        lenient().when(location.getWorld()).thenReturn(world);
        lenient().when(location.getBlockX()).thenReturn(0);
        lenient().when(location.getBlockY()).thenReturn(64);
        lenient().when(location.getBlockZ()).thenReturn(0);
        lenient().when(itemStack.getType()).thenReturn(Material.COBBLESTONE);
        lenient().when(blockConfigService.isCodeBlock(any(Material.class))).thenReturn(true);
        lenient().when(blockConfigService.getBlockConfigByMaterial(any(Material.class)))
            .thenReturn(new BlockConfigService.BlockConfig("COBBLESTONE", 
                org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                    new java.io.StringReader(
                        "name: Action Block\n" +
                        "type: ACTION\n" +
                        "default_action: sendMessage\n"
                    )
                ).createSection("test")));

        // Create BlockPlacementHandler instance
        blockPlacementHandler = new BlockPlacementHandler(plugin);
    }

    @Test
    void testConstructor() {
        // Test that the constructor creates a valid instance
        assertNotNull(blockPlacementHandler);
    }

    @Test
    void testGetAllCodeBlocks() {
        // Test getting all code blocks map
        Map<Location, CodeBlock> codeBlocks = blockPlacementHandler.getAllCodeBlocks();
        
        // Should not be null
        assertNotNull(codeBlocks);
    }

    @Test
    void testGetCodeBlock() {
        // Test getting a code block that doesn't exist
        CodeBlock codeBlock = blockPlacementHandler.getCodeBlock(location);
        
        // Should return null for non-existent block
        assertNull(codeBlock);
    }

    @Test
    void testOnBlockPlaceWithCodeBlock() {
        // Create a block place event
        BlockPlaceEvent event = new BlockPlaceEvent(block, null, null, itemStack, player, true, null);
        
        // Test the event handling
        assertDoesNotThrow(() -> blockPlacementHandler.onBlockPlace(event));
    }

    @Test
    void testOnBlockPlaceWithNonCodeBlock() {
        // Set up the block config service to return false for this material
        lenient().when(blockConfigService.isCodeBlock(Material.STONE)).thenReturn(false);
        lenient().when(block.getType()).thenReturn(Material.STONE);
        
        // Create a block place event with non-code block
        BlockPlaceEvent event = new BlockPlaceEvent(block, null, null, new ItemStack(Material.STONE), player, true, null);
        
        // Test the event handling
        assertDoesNotThrow(() -> blockPlacementHandler.onBlockPlace(event));
    }

    @Test
    void testOnPlayerInteractWithCodeBlock() {
        // Create a player interact event
        PlayerInteractEvent event = new PlayerInteractEvent(player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, itemStack, block, org.bukkit.block.BlockFace.UP);
        
        // Test the event handling
        assertDoesNotThrow(() -> blockPlacementHandler.onPlayerInteract(event));
    }

    @Test
    void testOnPlayerInteractWithNonCodeBlock() {
        // Set up the block config service to return false for this material
        lenient().when(blockConfigService.isCodeBlock(Material.STONE)).thenReturn(false);
        lenient().when(block.getType()).thenReturn(Material.STONE);
        
        // Create a player interact event with non-code block
        PlayerInteractEvent event = new PlayerInteractEvent(player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, itemStack, block, org.bukkit.block.BlockFace.UP);
        
        // Test the event handling
        assertDoesNotThrow(() -> blockPlacementHandler.onPlayerInteract(event));
    }

    @Test
    void testOnBlockBreakWithCodeBlock() {
        // Create a block break event
        org.bukkit.event.block.BlockBreakEvent event = new org.bukkit.event.block.BlockBreakEvent(block, player);
        
        // Test the event handling
        assertDoesNotThrow(() -> blockPlacementHandler.onBlockBreak(event));
    }

    @Test
    void testOnBlockBreakWithNonCodeBlock() {
        // Set up the block config service to return false for this material
        lenient().when(blockConfigService.isCodeBlock(Material.STONE)).thenReturn(false);
        lenient().when(block.getType()).thenReturn(Material.STONE);
        
        // Create a block break event with non-code block
        org.bukkit.event.block.BlockBreakEvent event = new org.bukkit.event.block.BlockBreakEvent(block, player);
        
        // Test the event handling
        assertDoesNotThrow(() -> blockPlacementHandler.onBlockBreak(event));
    }

    @Test
    void testHasCodeBlock() {
        // Test checking if a location has a code block (should be false initially)
        assertFalse(blockPlacementHandler.hasCodeBlock(location));
    }

    @Test
    void testGetBlockCodeBlocks() {
        // Test getting all code blocks (alternative method)
        Map<Location, CodeBlock> codeBlocks = blockPlacementHandler.getBlockCodeBlocks();
        
        // Should not be null
        assertNotNull(codeBlocks);
    }
}