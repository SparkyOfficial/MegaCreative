package com.megacreative.managers;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.ICodingManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
import com.megacreative.utils.ConfigManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorldManagerImplTest {

    @Mock
    private Plugin plugin;

    @Mock
    private ICodingManager codingManager;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Player player;

    @Mock
    private MegaCreative megaCreative;

    private WorldManagerImpl worldManager;

    @BeforeEach
    void setUp() {
        // Set up mocks
        lenient().when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        lenient().when(configManager.getMaxWorldsPerPlayer()).thenReturn(5);
        lenient().when(configManager.getWorldBorderSize()).thenReturn(300);
        lenient().when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(player.getName()).thenReturn("TestPlayer");

        // Create WorldManagerImpl instance
        worldManager = new WorldManagerImpl(plugin, codingManager, configManager);
    }

    @Test
    void testConstructorWithDependencies() {
        // Test that the constructor properly initializes fields
        assertNotNull(worldManager);
        // We can't directly access private fields, but we can test behavior
    }

    @Test
    void testConstructorWithConfigManager() {
        // Test the constructor that only takes ConfigManager
        WorldManagerImpl manager = new WorldManagerImpl(configManager);
        assertNotNull(manager);
        
        // Test setting plugin
        manager.setPlugin(plugin);
        // We can't directly verify the plugin is set, but we can test behavior
    }

    @Test
    void testGetWorld() {
        // Arrange
        String worldId = "test-world-id";
        
        // Act
        CreativeWorld result = worldManager.getWorld(worldId);
        
        // Assert
        assertNull(result); // Should return null for non-existent world
    }

    @Test
    void testGetWorldByName() {
        // Arrange
        String worldName = "TestWorld";
        
        // Act
        CreativeWorld result = worldManager.getWorldByName(worldName);
        
        // Assert
        assertNull(result); // Should return null for non-existent world
    }

    @Test
    void testGetPlayerWorlds() {
        // Act
        List<CreativeWorld> result = worldManager.getPlayerWorlds(player);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should return empty list for player with no worlds
    }

    @Test
    void testGetAllPublicWorlds() {
        // Act
        List<CreativeWorld> result = worldManager.getAllPublicWorlds();
        
        // Assert
        assertNotNull(result);
        // Initially should be empty
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetPlayerWorldCount() {
        // Act
        int result = worldManager.getPlayerWorldCount(player);
        
        // Assert
        assertEquals(0, result); // Should return 0 for player with no worlds
    }

    @Test
    void testGetCreativeWorlds() {
        // Act
        List<CreativeWorld> result = worldManager.getCreativeWorlds();
        
        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty()); // Should return empty list initially
    }

    @Test
    void testSaveWorld() {
        // Arrange
        CreativeWorld world = mock(CreativeWorld.class);
        when(world.getId()).thenReturn("test-world-id");
        
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.saveWorld(world));
    }

    @Test
    void testSaveAllWorlds() {
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.saveAllWorlds());
    }

    @Test
    void testSwitchToDevWorld() {
        // Arrange
        String worldId = "test-world-id";
        
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.switchToDevWorld(player, worldId));
    }

    @Test
    void testSwitchToPlayWorld() {
        // Arrange
        String worldId = "test-world-id";
        
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.switchToPlayWorld(player, worldId));
    }

    @Test
    void testSwitchToBuildWorld() {
        // Arrange
        String worldId = "test-world-id";
        
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.switchToBuildWorld(player, worldId));
    }

    @Test
    void testGetPairedWorld() {
        // Arrange
        CreativeWorld world = mock(CreativeWorld.class);
        
        // Act
        CreativeWorld result = worldManager.getPairedWorld(world);
        
        // Assert
        assertNull(result); // Should return null for world with no pair
    }

    @Test
    void testInitialize() {
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.initialize());
    }

    @Test
    void testShutdown() {
        // Act & Assert
        assertDoesNotThrow(() -> worldManager.shutdown());
    }
}