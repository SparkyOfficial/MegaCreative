package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.PlayerEventsListener;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.CreativeWorldType;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IntegrationVerificationTest {

    @Mock
    private MegaCreative plugin;
    
    @Mock
    private ScriptEngine scriptEngine;
    
    @Mock
    private BlockConfigService blockConfigService;
    
    @Mock
    private Player player;
    
    @Mock
    private World world;
    
    @Mock
    private com.megacreative.interfaces.IWorldManager worldManager;
    
    private PlayerEventsListener playerEventsListener;
    private CreativeWorld creativeWorld;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        // Setup mocks
        when(plugin.getServiceRegistry()).thenReturn(mock(com.megacreative.core.ServiceRegistry.class));
        when(plugin.getServiceRegistry().getService(ScriptEngine.class)).thenReturn(scriptEngine);
        when(plugin.getServiceRegistry().getBlockConfigService()).thenReturn(blockConfigService);
        when(plugin.getWorldManager()).thenReturn(worldManager);
        when(plugin.getLogger()).thenReturn(java.util.logging.Logger.getLogger("test"));
        
        when(player.getWorld()).thenReturn(world);
        when(player.getUniqueId()).thenReturn(UUID.randomUUID());
        when(world.getName()).thenReturn("test_world_dev");
        
        // Create creative world
        creativeWorld = new CreativeWorld("test_world", "test_world", UUID.randomUUID(), "test_owner", CreativeWorldType.FLAT);
        creativeWorld.setScripts(new ArrayList<>());
        
        when(worldManager.findCreativeWorldByBukkit(world)).thenReturn(creativeWorld);
        
        // Create listener
        playerEventsListener = new PlayerEventsListener(plugin);
    }
    
    @Test
    void testEventSystemIntegration() {
        // Create an event block
        CodeBlock eventBlock = new CodeBlock(Material.DIAMOND_BLOCK, "onJoin");
        
        // Create a script with the event block
        CodeScript script = new CodeScript("Test Join Script", true, eventBlock);
        script.setType(CodeScript.ScriptType.EVENT);
        
        // Add script to creative world
        creativeWorld.getScripts().add(script);
        
        // Create player join event
        PlayerJoinEvent joinEvent = new PlayerJoinEvent(player, "test player joined");
        
        // Process the event
        playerEventsListener.onPlayerJoin(joinEvent);
        
        // Verify script engine was called
        verify(scriptEngine, times(1)).executeScript(eq(script), eq(player), eq("player_join"));
    }
}