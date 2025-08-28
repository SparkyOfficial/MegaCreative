package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DevWorldProtectionListenerTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private Player player;

    @Mock
    private World world;

    private DevWorldProtectionListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        listener = new DevWorldProtectionListener(plugin);
        
        // Mock plugin getServiceRegistry and getBlockConfigService
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        
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
    void testIsCodeBlock() {
        // Test with a code block material (should return true)
        when(blockConfigService.isCodeBlock(Material.DIAMOND_BLOCK)).thenReturn(true);
        assertTrue(listener.isCodeBlock(Material.DIAMOND_BLOCK));
        
        // Test with a non-code block material (should return false)
        when(blockConfigService.isCodeBlock(Material.STONE)).thenReturn(false);
        assertFalse(listener.isCodeBlock(Material.STONE));
    }
}