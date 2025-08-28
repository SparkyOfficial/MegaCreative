package com.megacreative.listeners;

import com.megacreative.services.BlockConfigService;
import com.megacreative.managers.TrustedPlayerManager;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.logging.Logger;
import java.util.logging.LogManager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

class DevWorldProtectionListenerTest {

    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private TrustedPlayerManager trustedPlayerManager;
    
    @Mock
    private Player player;

    @Mock
    private World world;

    private DevWorldProtectionListener listener;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Create a simple test plugin
        Plugin testPlugin = new SimpleTestPlugin();
        
        // Use the new constructor with dependencies
        listener = new DevWorldProtectionListener(testPlugin, trustedPlayerManager, blockConfigService);
        
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
    
    // Simple test plugin implementation
    private static class SimpleTestPlugin implements Plugin {
        @Override
        public Logger getLogger() {
            return LogManager.getLogManager().getLogger("");
        }
        
        // Implement other required methods with minimal functionality
        @Override public void onLoad() {}
        @Override public void onEnable() {}
        @Override public void onDisable() {}
        @Override public boolean isEnabled() { return true; }
        @Override public String getName() { return "TestPlugin"; }
        @Override public org.bukkit.Server getServer() { return null; }
        @Override public org.bukkit.plugin.PluginLoader getPluginLoader() { return null; }
        @Override public org.bukkit.plugin.PluginDescriptionFile getDescription() { return null; }
        @Override public org.bukkit.configuration.file.FileConfiguration getConfig() { return null; }
        @Override public void saveConfig() {}
        @Override public void saveDefaultConfig() {}
        @Override public void saveResource(String resourcePath, boolean replace) {}
        @Override public java.io.InputStream getResource(String filename) { return null; }
        @Override public void reloadConfig() {}
        @Override public org.bukkit.plugin.PluginManager getPluginManager() { return null; }
        @Override public org.bukkit.scheduler.BukkitScheduler getScheduler() { return null; }
        @Override public org.bukkit.command.CommandMap getCommandMap() { return null; }
        @Override public org.bukkit.command.ConsoleCommandSender getConsole() { return null; }
        @Override public org.bukkit.permissions.Permissible getPermissible() { return null; }
        @Override public org.bukkit.permissions.PermissionManager getPermissionManager() { return null; }
        @Override public org.bukkit.generator.ChunkGenerator getDefaultWorldGenerator() { return null; }
        @Override public org.bukkit.generator.BiomeProvider getDefaultBiomeProvider() { return null; }
        @Override public boolean isNaggable() { return false; }
        @Override public void setNaggable(boolean canNag) {}
        @Override public org.bukkit.NamespacedKey getNamespacedKey() { return null; }
        @Override public org.bukkit.plugin.IllegalPluginAccessException illegalPluginAccessException(String message) { return null; }
        @Override public org.bukkit.plugin.UnknownDependencyException unknownDependencyException(String message) { return null; }
    }
}