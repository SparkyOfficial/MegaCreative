package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.BlockConfigService;
import com.megacreative.interfaces.ITrustedPlayerManager;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Piston;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.block.Sign;
import java.util.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlockPlacementHandlerNewTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private ITrustedPlayerManager trustedPlayerManager;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Mock
    private Block block;

    @Mock
    private Location location;

    @Mock
    private Block glassBlock;

    private BlockPlacementHandler handler;

    @BeforeEach
    void setUp() {
        // Initialize mocks
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(serviceRegistry.getTrustedPlayerManager()).thenReturn(trustedPlayerManager);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(player.getWorld()).thenReturn(world);
        lenient().when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(location.getWorld()).thenReturn(world);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(anyDouble(), anyDouble(), anyDouble())).thenReturn(location);
        lenient().when(world.getName()).thenReturn("megacreative_test_dev");
        
        // Mock logger to avoid null pointer exceptions
        Logger logger = mock(Logger.class);
        lenient().when(plugin.getLogger()).thenReturn(logger);

        handler = new BlockPlacementHandler(plugin);
    }

    @Test
    void testIsInDevWorld() {
        // Test with dev world name
        lenient().when(world.getName()).thenReturn("megacreative_123456_dev");
        assertTrue(handler.isInDevWorld(player));

        // Test with non-dev world name
        lenient().when(world.getName()).thenReturn("normal_world");
        assertFalse(handler.isInDevWorld(player));
    }

    @Test
    void testOnBlockPlace_NotInDevWorld() {
        // Arrange
        lenient().when(world.getName()).thenReturn("normal_world");
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(event.getBlockPlaced()).thenReturn(block);

        // Act
        handler.onBlockPlace(event);

        // Assert
        verify(event, never()).setCancelled(true);
    }

    @Test
    void testOnBlockPlace_NoGlassUnder() {
        // Arrange
        lenient().when(glassBlock.getType()).thenReturn(Material.AIR);
        lenient().when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(glassBlock);
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(event.getBlockPlaced()).thenReturn(block);
        lenient().when(block.getX()).thenReturn(0);
        lenient().when(block.getY()).thenReturn(64);
        lenient().when(block.getZ()).thenReturn(0);

        // Act
        handler.onBlockPlace(event);

        // Assert
        verify(event).setCancelled(true);
        verify(player).sendMessage(contains("Вы можете размещать блоки кода только на синее"));
    }

    @Test
    void testOnBlockPlace_BlueGlassUnder_CodeBlock() {
        // Arrange
        lenient().when(glassBlock.getType()).thenReturn(Material.BLUE_STAINED_GLASS);
        lenient().when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(glassBlock);
        lenient().when(blockConfigService.isCodeBlock(any(Material.class))).thenReturn(true);
        lenient().when(block.getType()).thenReturn(Material.DIAMOND_BLOCK);
        
        BlockConfigService.BlockConfig config = mock(BlockConfigService.BlockConfig.class);
        lenient().when(config.isConstructor()).thenReturn(false);
        lenient().when(config.getDisplayName()).thenReturn("Test Block");
        lenient().when(config.getDefaultAction()).thenReturn("test_action");
        lenient().when(blockConfigService.getBlockConfigByMaterial(Material.DIAMOND_BLOCK)).thenReturn(config);
        
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(event.getBlockPlaced()).thenReturn(block);
        lenient().when(block.getX()).thenReturn(0);
        lenient().when(block.getY()).thenReturn(64);
        lenient().when(block.getZ()).thenReturn(0);
        ItemStack itemInHand = new ItemStack(Material.DIAMOND_BLOCK);
        lenient().when(event.getItemInHand()).thenReturn(itemInHand);

        // Mock sign block to avoid null pointer
        Location signLoc = mock(Location.class);
        Block signBlock = mock(Block.class);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(0, 1, 0)).thenReturn(signLoc);
        lenient().when(signLoc.getBlock()).thenReturn(signBlock);
        lenient().when(signBlock.getType()).thenReturn(Material.AIR);

        // Act
        handler.onBlockPlace(event);

        // Assert
        verify(event, never()).setCancelled(true);
        verify(player).sendMessage(contains("Блок кода размещен"));
    }

    @Test
    void testOnBlockPlace_PistonBlock() {
        // Arrange
        lenient().when(glassBlock.getType()).thenReturn(Material.BLUE_STAINED_GLASS);
        lenient().when(world.getBlockAt(anyInt(), anyInt(), anyInt())).thenReturn(glassBlock);
        lenient().when(blockConfigService.isCodeBlock(any(Material.class))).thenReturn(false);
        lenient().when(block.getType()).thenReturn(Material.PISTON);
        
        BlockPlaceEvent event = mock(BlockPlaceEvent.class);
        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(event.getBlockPlaced()).thenReturn(block);
        lenient().when(block.getX()).thenReturn(0);
        lenient().when(block.getY()).thenReturn(64);
        lenient().when(block.getZ()).thenReturn(0);
        ItemStack itemInHand = new ItemStack(Material.PISTON);
        lenient().when(event.getItemInHand()).thenReturn(itemInHand);

        // Mock BlockData for piston
        Piston pistonData = mock(Piston.class);
        lenient().when(block.getBlockData()).thenReturn(pistonData);
        lenient().when(pistonData.clone()).thenReturn(pistonData);
        lenient().when(pistonData.getFacing()).thenReturn(BlockFace.NORTH);

        // Mock sign block to avoid null pointer
        Location signLoc = mock(Location.class);
        Block signBlock = mock(Block.class);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(0, 1, 0)).thenReturn(signLoc);
        lenient().when(signLoc.getBlock()).thenReturn(signBlock);
        lenient().when(signBlock.getType()).thenReturn(Material.AIR);

        // Act
        handler.onBlockPlace(event);

        // Assert
        verify(event, never()).setCancelled(true);
        assertNotNull(handler.getCodeBlock(location));
        assertEquals(CodeBlock.BracketType.OPEN, handler.getCodeBlock(location).getBracketType());
    }

    @Test
    void testOnBlockBreak_CodeBlock() {
        // Arrange
        CodeBlock codeBlock = new CodeBlock(Material.DIAMOND_BLOCK, "test_action");
        // Use reflection to access the private blockCodeBlocks field
        try {
            java.lang.reflect.Field field = BlockPlacementHandler.class.getDeclaredField("blockCodeBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Location, CodeBlock> blockCodeBlocks = 
                (java.util.Map<Location, CodeBlock>) field.get(handler);
            blockCodeBlocks.put(location, codeBlock);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Mock the sign block that will be removed
        Location signLoc = mock(Location.class);
        Block signBlock = mock(Block.class);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(0, 1, 0)).thenReturn(signLoc);
        lenient().when(signLoc.getBlock()).thenReturn(signBlock);
        lenient().when(signBlock.getType()).thenReturn(Material.AIR);

        BlockBreakEvent event = mock(BlockBreakEvent.class);
        lenient().when(event.getBlock()).thenReturn(block);
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(event.getPlayer()).thenReturn(player);

        // Act
        handler.onBlockBreak(event);

        // Assert
        verify(player).sendMessage(contains("Блок кода удален!"));
        // Check that the block was removed from the map
        try {
            java.lang.reflect.Field field = BlockPlacementHandler.class.getDeclaredField("blockCodeBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Location, CodeBlock> blockCodeBlocks = 
                (java.util.Map<Location, CodeBlock>) field.get(handler);
            assertFalse(blockCodeBlocks.containsKey(location));
        } catch (Exception e) {
            fail("Failed to verify test result: " + e.getMessage());
        }
    }

    @Test
    void testOnBlockBreak_PistonBracket() {
        // Arrange
        lenient().when(block.getType()).thenReturn(Material.PISTON);
        
        // Mock the sign block that will be removed
        Location signLoc = mock(Location.class);
        Block signBlock = mock(Block.class);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(0, 1, 0)).thenReturn(signLoc);
        lenient().when(signLoc.getBlock()).thenReturn(signBlock);
        lenient().when(signBlock.getType()).thenReturn(Material.AIR);
        
        BlockBreakEvent event = mock(BlockBreakEvent.class);
        lenient().when(event.getBlock()).thenReturn(block);
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(event.getPlayer()).thenReturn(player);

        // Act
        handler.onBlockBreak(event);

        // Assert
        verify(player).sendMessage(contains("Скобка удалена!"));
    }

    @Test
    void testOnPlayerInteract_CodeBlock() {
        // Arrange
        CodeBlock codeBlock = new CodeBlock(Material.DIAMOND_BLOCK, "test_action");
        // Use reflection to access the private blockCodeBlocks field
        try {
            java.lang.reflect.Field field = BlockPlacementHandler.class.getDeclaredField("blockCodeBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Location, CodeBlock> blockCodeBlocks = 
                (java.util.Map<Location, CodeBlock>) field.get(handler);
            blockCodeBlocks.put(location, codeBlock);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(event.getClickedBlock()).thenReturn(block);
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(world.getName()).thenReturn("megacreative_test_dev");
        lenient().when(event.getAction()).thenReturn(org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK);
        lenient().when(event.getHand()).thenReturn(EquipmentSlot.HAND);

        // Mock player inventory to avoid null pointer
        PlayerInventory inventory = mock(PlayerInventory.class);
        lenient().when(player.getInventory()).thenReturn(inventory);
        lenient().when(inventory.getItemInMainHand()).thenReturn(new ItemStack(Material.AIR));

        // Mock sign block to avoid null pointer
        Location signLoc = mock(Location.class);
        Block signBlock = mock(Block.class);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(0, 1, 0)).thenReturn(signLoc);
        lenient().when(signLoc.getBlock()).thenReturn(signBlock);
        lenient().when(signBlock.getType()).thenReturn(Material.AIR);

        // Act
        handler.onPlayerInteract(event);

        // Note: We can't easily verify GUI opening without more complex mocking
        // But we can verify that the method doesn't throw exceptions
        assertDoesNotThrow(() -> handler.onPlayerInteract(event));
    }

    @Test
    void testOnPlayerInteract_PistonBracket() {
        // Arrange
        CodeBlock codeBlock = new CodeBlock(Material.PISTON, "BRACKET");
        codeBlock.setBracketType(CodeBlock.BracketType.OPEN);
        
        // Use reflection to access the private blockCodeBlocks field
        try {
            java.lang.reflect.Field field = BlockPlacementHandler.class.getDeclaredField("blockCodeBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Location, CodeBlock> blockCodeBlocks = 
                (java.util.Map<Location, CodeBlock>) field.get(handler);
            blockCodeBlocks.put(location, codeBlock);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        lenient().when(block.getType()).thenReturn(Material.PISTON);
        
        PlayerInteractEvent event = mock(PlayerInteractEvent.class);
        lenient().when(event.getPlayer()).thenReturn(player);
        lenient().when(event.getClickedBlock()).thenReturn(block);
        lenient().when(block.getLocation()).thenReturn(location);
        lenient().when(world.getName()).thenReturn("megacreative_test_dev");
        lenient().when(event.getAction()).thenReturn(org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK);
        lenient().when(event.getHand()).thenReturn(EquipmentSlot.HAND);

        // Mock BlockData for piston
        Piston pistonData = mock(Piston.class);
        lenient().when(block.getBlockData()).thenReturn(pistonData);
        lenient().when(pistonData.clone()).thenReturn(pistonData);
        lenient().when(pistonData.getFacing()).thenReturn(BlockFace.NORTH);

        // Mock player inventory to avoid null pointer
        PlayerInventory inventory = mock(PlayerInventory.class);
        lenient().when(player.getInventory()).thenReturn(inventory);
        lenient().when(inventory.getItemInMainHand()).thenReturn(new ItemStack(Material.AIR));

        // Mock sign block to avoid null pointer
        Location signLoc = mock(Location.class);
        Block signBlock = mock(Block.class);
        lenient().when(location.clone()).thenReturn(location);
        lenient().when(location.add(0, 1, 0)).thenReturn(signLoc);
        lenient().when(signLoc.getBlock()).thenReturn(signBlock);
        lenient().when(signBlock.getType()).thenReturn(Material.AIR);

        // Act
        handler.onPlayerInteract(event);

        // Assert
        verify(player).sendMessage(contains("Тип скобки изменен"));
        // Verify that the bracket type was toggled
        assertEquals(CodeBlock.BracketType.CLOSE, handler.getCodeBlock(location).getBracketType());
    }

    @Test
    void testCleanUpPlayerData() {
        // Arrange
        UUID playerId = UUID.randomUUID();
        
        // Use reflection to set player data
        try {
            java.lang.reflect.Field visField = BlockPlacementHandler.class.getDeclaredField("playerVisualizationStates");
            visField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, Boolean> playerVisualizationStates = 
                (java.util.Map<UUID, Boolean>) visField.get(handler);
            playerVisualizationStates.put(playerId, true);
            
            java.lang.reflect.Field debugField = BlockPlacementHandler.class.getDeclaredField("playerDebugStates");
            debugField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, Boolean> playerDebugStates = 
                (java.util.Map<UUID, Boolean>) debugField.get(handler);
            playerDebugStates.put(playerId, true);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        handler.cleanUpPlayerData(playerId);

        // Assert
        try {
            java.lang.reflect.Field visField = BlockPlacementHandler.class.getDeclaredField("playerVisualizationStates");
            visField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, Boolean> playerVisualizationStates = 
                (java.util.Map<UUID, Boolean>) visField.get(handler);
            assertFalse(playerVisualizationStates.containsKey(playerId));
            
            java.lang.reflect.Field debugField = BlockPlacementHandler.class.getDeclaredField("playerDebugStates");
            debugField.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<UUID, Boolean> playerDebugStates = 
                (java.util.Map<UUID, Boolean>) debugField.get(handler);
            assertFalse(playerDebugStates.containsKey(playerId));
        } catch (Exception e) {
            fail("Failed to verify test result: " + e.getMessage());
        }
    }

    @Test
    void testHasCodeBlock() {
        // Arrange
        CodeBlock codeBlock = new CodeBlock(Material.DIAMOND_BLOCK, "test_action");
        // Use reflection to access the private blockCodeBlocks field
        try {
            java.lang.reflect.Field field = BlockPlacementHandler.class.getDeclaredField("blockCodeBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Location, CodeBlock> blockCodeBlocks = 
                (java.util.Map<Location, CodeBlock>) field.get(handler);
            blockCodeBlocks.put(location, codeBlock);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act & Assert
        assertTrue(handler.hasCodeBlock(location));
    }

    @Test
    void testGetCodeBlock() {
        // Arrange
        CodeBlock codeBlock = new CodeBlock(Material.DIAMOND_BLOCK, "test_action");
        // Use reflection to access the private blockCodeBlocks field
        try {
            java.lang.reflect.Field field = BlockPlacementHandler.class.getDeclaredField("blockCodeBlocks");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            java.util.Map<Location, CodeBlock> blockCodeBlocks = 
                (java.util.Map<Location, CodeBlock>) field.get(handler);
            blockCodeBlocks.put(location, codeBlock);
        } catch (Exception e) {
            fail("Failed to set up test: " + e.getMessage());
        }

        // Act
        CodeBlock result = handler.getCodeBlock(location);

        // Assert
        assertNotNull(result);
        assertEquals("test_action", result.getAction());
    }
}