package com.megacreative.coding.events.test;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.*;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for advanced custom event features
 */
public class AdvancedCustomEventTest {
    
    private CustomEventManager eventManager;
    private MegaCreative mockPlugin;
    
    @BeforeEach
    public void setUp() {
        mockPlugin = Mockito.mock(MegaCreative.class);
        eventManager = new CustomEventManager(mockPlugin);
    }
    
    @Test
    public void testAdvancedEventTrigger() {
        // Create a simple event
        CustomEventBuilder eventBuilder = new CustomEventBuilder("testEvent")
            .description("A test event for advanced triggering")
            .category("test")
            .requiredField("message", String.class, "Test message");
        CustomEvent testEvent = eventBuilder.buildAndRegister(eventManager);
        
        // Create an advanced trigger
        Map<String, DataValue> triggerData = new HashMap<>();
        triggerData.put("message", DataValue.fromObject("Hello from advanced trigger!"));
        
        AdvancedEventTrigger trigger = new AdvancedEventTrigger.Builder("testEvent")
            .triggerId("test-trigger-1")
            .eventData(triggerData)
            .delay(100) // 100ms delay
            .repeat(3, 500) // Repeat 3 times with 500ms interval
            .build();
        
        eventManager.registerAdvancedTrigger(trigger);
        
        // Verify trigger was registered
        assertNotNull(eventManager.getAdvancedTrigger("test-trigger-1"));
        
        // Execute the trigger
        Player mockPlayer = Mockito.mock(Player.class);
        when(mockPlayer.getUniqueId()).thenReturn(UUID.randomUUID());
        
        eventManager.executeAdvancedTrigger("test-trigger-1", mockPlayer, "world");
        
        // Verify trigger execution (in a real test, we would verify event handling)
        assertEquals(1, trigger.getExecutionCount());
    }
    
    @Test
    public void testEventInheritance() {
        // Create a base player event
        CustomEventBuilder baseEventBuilder = new CustomEventBuilder("basePlayerEvent")
            .description("Base player event")
            .requiredField("player", Player.class, "The player");
        CustomEvent basePlayerEvent = baseEventBuilder.buildAndRegister(eventManager);
        
        // Create a derived event that inherits from basePlayerEvent
        CustomEvent derivedEvent = new CustomEvent("derivedPlayerEvent", "test");
        derivedEvent.setDescription("Derived player event");
        derivedEvent.inheritFrom(basePlayerEvent);
        derivedEvent.addDataField("action", String.class, true, "Player action");
        
        eventManager.registerEvent(derivedEvent);
        
        // Verify inheritance
        assertTrue(derivedEvent.isCompatibleWith("basePlayerEvent"));
        assertEquals("basePlayerEvent", derivedEvent.getParentEvent());
        
        // Verify inherited fields
        assertTrue(derivedEvent.getDataFields().containsKey("player"));
        assertTrue(derivedEvent.getDataFields().containsKey("action"));
    }
    
    @Test
    public void testEventPatternDetection() {
        // Create a simple pattern: playerJoin -> playerChat
        EventCorrelationEngine.PatternCompletionListener listener = mock(EventCorrelationEngine.PatternCompletionListener.class);
        eventManager.getCorrelationEngine().addCompletionListener(listener);
        
        EventCorrelationEngine.EventPattern pattern = new EventCorrelationEngine.EventPattern.Builder("joinThenChat")
            .patternId("joinThenChat") // Set explicit pattern ID
            .description("Player joins then chats")
            .addStep("playerJoin")
            .addStep("playerChat")
            .timeout(5000) // 5 second timeout
            .build();
        
        eventManager.getCorrelationEngine().registerPattern(pattern);
        
        // Verify pattern was registered
        assertNotNull(eventManager.getCorrelationEngine().getPattern("joinThenChat"));
        
        // In a real test, we would simulate events and verify pattern detection
        // For now, we just verify the setup
    }
    
    @Test
    public void testEventFiltering() {
        // Create test event
        CustomEventBuilder eventBuilder = new CustomEventBuilder("filterTestEvent")
            .description("Event for testing filters")
            .requiredField("value", Integer.class, "Test value");
        CustomEvent testEvent = eventBuilder.buildAndRegister(eventManager);
        
        // Test data transformation
        Map<String, DataValue> originalData = new HashMap<>();
        originalData.put("value", DataValue.fromObject(5));
        
        Map<String, java.util.function.Function<DataValue, DataValue>> transformations = new HashMap<>();
        transformations.put("value", dataValue -> DataValue.fromObject(((Integer) dataValue.getValue()) * 2));
        
        Map<String, DataValue> transformedData = eventManager.transformEventData(originalData, transformations);
        
        assertEquals(10, transformedData.get("value").getValue());
    }
}