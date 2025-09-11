package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
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
    private com.megacreative.interfaces.IWorldManager worldManager;
    
    private AutoConnectionManager autoConnectionManager;
    private CreativeWorld creativeWorld;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mocks
        when(plugin.getServiceRegistry()).thenReturn(mock(com.megacreative.core.ServiceRegistry.class));
        when(plugin.getServiceRegistry().getBlockConfigService()).thenReturn(blockConfigService);
        when(plugin.getWorldManager()).thenReturn(worldManager);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        
        when(player.getWorld()).thenReturn(world);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(world.getName()).thenReturn("test_world_dev");
        
        // Create creative world
        creativeWorld = new CreativeWorld("test_world", "test_world", UUID.randomUUID(), "test_owner", CreativeWorldType.FLAT);
        creativeWorld.setScripts(null); // Start with no scripts
        
        when(worldManager.findCreativeWorldByBukkit(world)).thenReturn(creativeWorld);
        
        // Create manager
        autoConnectionManager = new AutoConnectionManager(plugin, blockConfigService);
    }
    
    @Test
    void testIsEventBlock() {
        // Create event block config
        BlockConfigService.BlockConfig eventConfig = mock(BlockConfigService.BlockConfig.class);
        when(eventConfig.getType()).thenReturn("EVENT");
        when(blockConfigService.getBlockConfig("onJoin")).thenReturn(eventConfig);
        
        // Create event block
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Test
        assertTrue(autoConnectionManager.isEventBlock(eventBlock), "Block should be recognized as event block");
    }
    
    @Test
    void testIsNotEventBlock() {
        // Create action block config
        BlockConfigService.BlockConfig actionConfig = mock(BlockConfigService.BlockConfig.class);
        when(actionConfig.getType()).thenReturn("ACTION");
        when(blockConfigService.getBlockConfig("sendMessage")).thenReturn(actionConfig);
        
        // Create action block
        CodeBlock actionBlock = new CodeBlock(Material.COBBLESTONE, "sendMessage");
        
        // Test
        assertFalse(autoConnectionManager.isEventBlock(actionBlock), "Block should not be recognized as event block");
    }
    
    @Test
    void testCreateAndAddScript() {
        // Create event block config
        BlockConfigService.BlockConfig eventConfig = mock(BlockConfigService.BlockConfig.class);
        when(eventConfig.getType()).thenReturn("EVENT");
        when(blockConfigService.getBlockConfig("onJoin")).thenReturn(eventConfig);
        
        // Create event block
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Create location
        Location location = new Location(world, 0, 64, 0);
        
        // Call method
        autoConnectionManager.createAndAddScript(eventBlock, player, location);
        
        // Verify script was added
        assertNotNull(creativeWorld.getScripts(), "Scripts list should be initialized");
        assertEquals(1, creativeWorld.getScripts().size(), "One script should be added");
        
        CodeScript script = creativeWorld.getScripts().get(0);
        assertNotNull(script, "Script should not be null");
        assertEquals(eventBlock, script.getRootBlock(), "Script root block should match");
        assertTrue(script.isEnabled(), "Script should be enabled");
        assertEquals(CodeScript.ScriptType.EVENT, script.getType(), "Script type should be EVENT");
    }
    
    @Test
    void testRemoveScript() {
        // Create event block config
        BlockConfigService.BlockConfig eventConfig = mock(BlockConfigService.BlockConfig.class);
        when(eventConfig.getType()).thenReturn("EVENT");
        when(blockConfigService.getBlockConfig("onJoin")).thenReturn(eventConfig);
        
        // Create event block
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        eventBlock.setId(UUID.randomUUID()); // Set a specific ID
        
        // Initialize scripts list
        creativeWorld.setScripts(new java.util.ArrayList<>());
        
        // Create and add script
        CodeScript script = new CodeScript("Test Script", true, eventBlock);
        creativeWorld.getScripts().add(script);
        
        // Create location
        Location location = new Location(world, 0, 64, 0);
        
        // Call method
        autoConnectionManager.removeScript(eventBlock, location);
        
        // Verify script was removed
        assertEquals(0, creativeWorld.getScripts().size(), "Script should be removed");
    }
    
    @Test
    void testCreateAndAddScriptWithNullScripts() {
        // Set scripts to null
        creativeWorld.setScripts(null);
        
        // Create event block config
        BlockConfigService.BlockConfig eventConfig = mock(BlockConfigService.BlockConfig.class);
        when(eventConfig.getType()).thenReturn("EVENT");
        when(blockConfigService.getBlockConfig("onJoin")).thenReturn(eventConfig);
        
        // Create event block
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Create location
        Location location = new Location(world, 0, 64, 0);
        
        // Call method
        autoConnectionManager.createAndAddScript(eventBlock, player, location);
        
        // Verify script was added
        assertNotNull(creativeWorld.getScripts(), "Scripts list should be initialized");
        assertEquals(1, creativeWorld.getScripts().size(), "One script should be added");
    }
}