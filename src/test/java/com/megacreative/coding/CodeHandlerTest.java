package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.activators.Activator;
import com.megacreative.coding.activators.PlayerJoinActivator;
import com.megacreative.coding.events.GameEvent;
import com.megacreative.models.CreativeWorld;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class CodeHandlerTest {
    
    @Mock
    private MegaCreative plugin;
    
    @Mock
    private CreativeWorld creativeWorld;
    
    @Mock
    private ScriptEngine scriptEngine;
    
    @Mock
    private Player player;
    
    private CodeHandler codeHandler;
    
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        codeHandler = new CodeHandler(plugin, creativeWorld);
    }
    
    @Test
    public void testRegisterAndUnregisterActivator() {
        // Create a mock activator
        Activator activator = mock(Activator.class);
        when(activator.getId()).thenReturn("test-activator-id");
        when(activator.getEventName()).thenReturn("onJoin");
        
        // Register the activator
        codeHandler.registerActivator(activator);
        
        // Verify the activator was registered
        assertEquals(1, codeHandler.getActivatorCount());
        assertNotNull(codeHandler.getActivator("test-activator-id"));
        
        // Unregister the activator
        codeHandler.unregisterActivator("test-activator-id");
        
        // Verify the activator was unregistered
        assertEquals(0, codeHandler.getActivatorCount());
        assertNull(codeHandler.getActivator("test-activator-id"));
    }
    
    @Test
    public void testHandleEventWithNoActivators() {
        // Handle an event with no registered activators
        GameEvent gameEvent = new GameEvent("onJoin");
        codeHandler.handleEvent("onJoin", gameEvent, player);
        
        // Should not throw any exceptions
        assertEquals(0, codeHandler.getActivatorCount());
    }
}