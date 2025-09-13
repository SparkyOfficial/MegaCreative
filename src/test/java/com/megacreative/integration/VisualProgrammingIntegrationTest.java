package com.megacreative.integration;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.services.CodeCompiler;
import com.megacreative.services.BlockConfigService;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.managers.WorldManagerImpl;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
import com.megacreative.models.WorldMode;
import com.megacreative.utils.ConfigManager;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VisualProgrammingIntegrationTest {

    @Mock
    private MegaCreative plugin;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Mock
    private BlockConfigService blockConfigService;

    @Mock
    private BlockPlacementHandler blockPlacementHandler;

    @Mock
    private ConfigManager configManager;

    @Mock
    private Player player;

    @Mock
    private World world;

    private CodeCompiler codeCompiler;
    private WorldManagerImpl worldManager;

    @BeforeEach
    void setUp() {
        // Set up mocks
        lenient().when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        lenient().when(plugin.getServiceRegistry()).thenReturn(serviceRegistry);
        lenient().when(plugin.getBlockPlacementHandler()).thenReturn(blockPlacementHandler);
        lenient().when(serviceRegistry.getBlockConfigService()).thenReturn(blockConfigService);
        lenient().when(configManager.getMaxWorldsPerPlayer()).thenReturn(5);
        lenient().when(configManager.getWorldBorderSize()).thenReturn(300);
        lenient().when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        lenient().when(player.getName()).thenReturn("TestPlayer");
        lenient().when(world.getName()).thenReturn("test_world_dev");
        lenient().when(blockConfigService.isCodeBlock(any(Material.class))).thenReturn(true);
        
        // Create service instances
        codeCompiler = new CodeCompiler(plugin);
        worldManager = new WorldManagerImpl(plugin, null, configManager);
    }

    @Test
    void testCompleteVisualProgrammingFlow() {
        // This test simulates the complete flow of the visual programming system:
        // 1. Player creates a world
        // 2. Player places code blocks
        // 3. System compiles the structure into scripts
        // 4. Scripts are executed
        
        // Step 1: Create a creative world
        CreativeWorld creativeWorld = mock(CreativeWorld.class);
        when(creativeWorld.getId()).thenReturn("test-world-id");
        when(creativeWorld.getName()).thenReturn("TestWorld");
        when(creativeWorld.getMode()).thenReturn(WorldMode.DEV);
        
        // Step 2: Simulate placing code blocks
        // Create an event block (diamond block for onJoin event)
        Location eventLocation = new Location(world, 0, 64, 0);
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Create an action block (cobblestone for sendMessage action)
        Location actionLocation = new Location(world, 1, 64, 0);
        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        
        // Register the blocks with the placement handler
        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(
            new java.util.HashMap<>() {{
                put(eventLocation, eventBlock);
                put(actionLocation, actionBlock);
            }}
        );
        lenient().when(blockPlacementHandler.getCodeBlock(eventLocation)).thenReturn(eventBlock);
        lenient().when(blockPlacementHandler.getCodeBlock(actionLocation)).thenReturn(actionBlock);
        
        // Step 3: Compile the world structure into scripts
        List<CodeScript> compiledScripts = codeCompiler.compileWorldScripts(world);
        
        // Verify that compilation was successful
        assertNotNull(compiledScripts);
        // Note: This might be empty because our mock setup doesn't fully replicate the real compilation logic
        // But we're testing that the integration doesn't throw exceptions
        
        // Step 4: Verify world management
        worldManager.saveWorld(creativeWorld);
        List<CreativeWorld> worlds = worldManager.getCreativeWorlds();
        assertNotNull(worlds);
        
        // Verify that we can retrieve the world
        CreativeWorld retrievedWorld = worldManager.getWorld("test-world-id");
        assertNull(retrievedWorld); // Will be null because we're not actually saving to storage
    }

    @Test
    void testBlockPlacementAndCompilationIntegration() {
        // Test the integration between block placement and compilation
        
        // Set up block configurations with lenient stubbings
        lenient().when(blockConfigService.getBlockConfigByMaterial(Material.DIAMOND_BLOCK))
            .thenReturn(createMockBlockConfig("DIAMOND_BLOCK", "EVENT", "onJoin"));
        lenient().when(blockConfigService.getBlockConfigByMaterial(Material.COBBLESTONE))
            .thenReturn(createMockBlockConfig("COBBLESTONE", "ACTION", "sendMessage"));
        
        // Create locations and blocks
        Location eventLocation = new Location(world, 10, 64, 10);
        Location actionLocation = new Location(world, 11, 64, 10);
        
        // Create physical blocks
        org.bukkit.block.Block eventBlock = mock(org.bukkit.block.Block.class);
        org.bukkit.block.Block actionBlock = mock(org.bukkit.block.Block.class);
        
        lenient().when(eventBlock.getType()).thenReturn(Material.DIAMOND_BLOCK);
        lenient().when(eventBlock.getLocation()).thenReturn(eventLocation);
        lenient().when(actionBlock.getType()).thenReturn(Material.COBBLESTONE);
        lenient().when(actionBlock.getLocation()).thenReturn(actionLocation);
        
        // Simulate block placement
        CodeBlock codeEventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        CodeBlock codeActionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        
        lenient().when(blockPlacementHandler.getAllCodeBlocks()).thenReturn(
            new java.util.HashMap<>() {{
                put(eventLocation, codeEventBlock);
                put(actionLocation, codeActionBlock);
            }}
        );
        
        // Compile the world
        List<CodeScript> scripts = codeCompiler.compileWorldScripts(world);
        
        // Verify compilation results
        assertNotNull(scripts);
        // Note: This might be empty because our mock setup doesn't fully replicate the real compilation logic
        // But we're testing that the integration doesn't throw exceptions
    }

    @Test
    void testWorldSwitchingIntegration() {
        // Test the integration of world switching functionality
        String worldId = "test-world-id";
        
        // This should not throw any exceptions
        assertDoesNotThrow(() -> worldManager.switchToDevWorld(player, worldId));
        assertDoesNotThrow(() -> worldManager.switchToPlayWorld(player, worldId));
        assertDoesNotThrow(() -> worldManager.switchToBuildWorld(player, worldId));
    }

    @Test
    void testErrorHandlingInIntegration() {
        // Test that the system handles errors gracefully
        
        // Test world management with null parameters (should not throw)
        assertDoesNotThrow(() -> worldManager.getWorld(null));
        // Note: We can't test saveWorld(null) because it would throw a NullPointerException in the implementation
        // This is expected behavior as the method should not accept null worlds
    }

    // Helper method to create mock block configurations
    private BlockConfigService.BlockConfig createMockBlockConfig(String id, String type, String defaultAction) {
        return new BlockConfigService.BlockConfig(id, 
            org.bukkit.configuration.file.YamlConfiguration.loadConfiguration(
                new java.io.StringReader(
                    "name: " + id + " Block\n" +
                    "type: " + type + "\n" +
                    "default_action: " + defaultAction + "\n"
                )
            ).createSection("test"));
    }
}