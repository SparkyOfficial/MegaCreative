package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.services.BlockConfigService;
import com.megacreative.managers.TrustedPlayerManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DevWorldProtectionListenerTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private TrustedPlayerManager trustedPlayerManager;
    
    @Mock
    private Logger logger;

    @Mock
    private Player player;

    @Mock
    private World world;

    private DevWorldProtectionListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Mock the logger
        when(plugin.getLogger()).thenReturn(logger);
        
        // Use the new constructor with dependencies
        listener = new DevWorldProtectionListener(plugin, trustedPlayerManager, blockConfigService);
        
        // Mock player and world
        when(player.getWorld()).thenReturn(world);
        when(world.getName()).thenReturn("test_dev");
    }

    @Test
    void testIsInDevWorld() {
        // Test with dev world name
        when(world.getName()).thenReturn("megacreative_123456_dev");
        assertTrue(listener.isInDevWorld(player));
        
        // Test with non-dev world name
        when(world.getName()).thenReturn("megacreative_123456");
        assertFalse(listener.isInDevWorld(player));
    }

    @Test
    void testIsMaterialAConfiguredCodeBlock() {
        // Test with a code block material (should return true)
        when(blockConfigService.isCodeBlock(Material.DIAMOND_BLOCK)).thenReturn(true);
        assertTrue(listener.isMaterialAConfiguredCodeBlock(Material.DIAMOND_BLOCK));
        
        // Test with a non-code block material (should return false)
        when(blockConfigService.isCodeBlock(Material.STONE)).thenReturn(false);
        assertFalse(listener.isMaterialAConfiguredCodeBlock(Material.STONE));
    }
    
    @Test
    void testIsMaterialPermittedInDevWorld() {
        // Test with a hardcoded allowed material
        assertTrue(listener.isMaterialPermittedInDevWorld(Material.ANVIL));
        
        // Test with a non-allowed material
        assertFalse(listener.isMaterialPermittedInDevWorld(Material.STONE));
    }
}