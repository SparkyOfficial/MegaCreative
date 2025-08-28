package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.BlockConfigService;
import com.megacreative.managers.TrustedPlayerManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class BlockPlacementHandlerTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private TrustedPlayerManager trustedPlayerManager;

    @Mock
    private Player player;

    @Mock
    private World world;

    @Mock
    private Block block;

    private BlockPlacementHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new BlockPlacementHandler(plugin);
        
        // Mock plugin services
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        when(plugin.getTrustedPlayerManager()).thenReturn(trustedPlayerManager);
        
        // Mock player and world
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("megacreative_123456_dev");
        when(block.getType()).thenReturn(Material.DIAMOND_BLOCK);
    }

    @Test
    void testIsInDevWorld() {
        // Test with dev world name
        when(world.getName()).thenReturn("megacreative_123456_dev");
        assertTrue(handler.isInDevWorld(player));
        
        // Test with non-dev world name
        when(world.getName()).thenReturn("megacreative_123456");
        assertFalse(handler.isInDevWorld(player));
    }

    @Test
    void testOnBlockPlaceWithCodeBlock() {
        // Test block placement in dev world with code block
        when(blockConfigService.isCodeBlock(Material.DIAMOND_BLOCK)).thenReturn(true);
        when(trustedPlayerManager.canCodeInDevWorld(player)).thenReturn(true);
        
        BlockPlaceEvent event = new BlockPlaceEvent(block, null, null, null, player, true, null);
        
        handler.onBlockPlace(event);
        
        // Event should not be cancelled since it's a valid code block in dev world
        assertFalse(event.isCancelled());
    }

    @Test
    void testOnBlockPlaceWithNonCodeBlock() {
        // Test block placement in dev world with non-code block
        when(blockConfigService.isCodeBlock(Material.STONE)).thenReturn(false);
        when(block.getType()).thenReturn(Material.STONE);
        
        BlockPlaceEvent event = new BlockPlaceEvent(block, null, null, null, player, true, null);
        
        handler.onBlockPlace(event);
        
        // Event should not be processed since it's not a code block
        assertFalse(event.isCancelled());
    }
}