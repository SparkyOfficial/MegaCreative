package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.values.types.TextValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Advanced Trigger Custom Event action with DataValue integration
 * Triggers custom events with dynamic data parameters
 * 
 * Parameters:
 * - "event_name": Name of the custom event to trigger
 * - "data": Map or list of data to pass to event handlers
 * - "global": Whether to trigger globally (default: false, world-scoped)
 * - "async": Whether to trigger asynchronously (default: false)
 * 
 * Examples:
 * - Trigger event "player_achievement" with data {"player": "${player}", "achievement": "first_login"}
 * - Trigger global event "server_message" with data {"message": "Welcome to the server!"}
 */
public class TriggerCustomEventAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        try {
            // Get event name
            DataValue eventNameValue = block.getParameter("event_name");
            String eventName = null;
            
            if (eventNameValue != null && !eventNameValue.isEmpty()) {
                DataValue resolvedName = resolver.resolve(context, eventNameValue);
                eventName = resolvedName.asString();
            } else {
                // Fallback to GUI slot
                var nameItem = block.getItemFromSlot("event_name_slot");
                if (nameItem != null && nameItem.hasItemMeta()) {
                    eventName = nameItem.getItemMeta().getDisplayName();
                }
            }
            
            if (eventName == null || eventName.trim().isEmpty()) {
                player.sendMessage("§c[TriggerEvent] No event name specified!");
                return;
            }
            
            eventName = eventName.trim();
            
            // Get CustomEventManager
            CustomEventManager eventManager = context.getPlugin().getServiceRegistry().getCustomEventManager();
            if (eventManager == null) {
                player.sendMessage("§c[TriggerEvent] Custom event system not available!");
                return;
            }
            
            // Check if event exists
            Map<String, CustomEvent> events = eventManager.getEvents();
            CustomEvent customEvent = events.get(eventName);
            if (customEvent == null) {
                player.sendMessage("§c[TriggerEvent] Custom event not found: " + eventName);
                return;
            }
            
            // Get event data
            Map<String, DataValue> eventData = collectEventData(context, block, resolver, customEvent);
            
            // Get trigger options
            boolean isGlobal = getBooleanParameter(block, resolver, context, "global", false);
            boolean isAsync = getBooleanParameter(block, resolver, context, "async", false);
            
            // Validate event data against event definition
            String validationError = validateEventData(customEvent, eventData);
            if (validationError != null) {
                player.sendMessage("§c[TriggerEvent] Invalid event data: " + validationError);
                return;
            }
            
            // Trigger the custom event
            triggerCustomEvent(eventManager, customEvent, eventData, context, isGlobal, isAsync);
            
            player.sendMessage("§a[TriggerEvent] Triggered event: " + eventName + 
                              (isGlobal ? " (global)" : " (world)") + 
                              (isAsync ? " (async)" : ""));
            
        } catch (Exception e) {
            player.sendMessage("§c[TriggerEvent] Error triggering event: " + e.getMessage());
            context.getPlugin().getLogger().warning("TriggerEvent execution error: " + e.getMessage());
        }
    }
    
    /**
     * Collects event data from block parameters and variables
     */
    private Map<String, DataValue> collectEventData(ExecutionContext context, CodeBlock block, 
                                                   ParameterResolver resolver, CustomEvent customEvent) {
        Map<String, DataValue> eventData = new HashMap<>();
        
        // Try to get data from "data" parameter (could be a variable reference to a map)
        DataValue dataValue = block.getParameter("data");
        if (dataValue != null && !dataValue.isEmpty()) {
            DataValue resolvedData = resolver.resolve(context, dataValue);
            // If it's a string that looks like JSON or variable reference, try to parse it
            // For now, we'll handle it as a simple string
            eventData.put("data", resolvedData);
        }
        
        // Collect data for each field defined in the custom event
        for (CustomEvent.EventDataField field : customEvent.getDataFields().values()) {
            String fieldName = field.getName();
            DataValue fieldValue = block.getParameter("data_" + fieldName);
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                DataValue resolvedField = resolver.resolve(context, fieldValue);
                eventData.put(fieldName, resolvedField);
            } else if (field.isRequired() && field.getDefaultValue() != null) {
                // Use default value for required fields
                eventData.put(fieldName, field.getDefaultValue());
            }
        }
        
        // Add standard context data
        eventData.put("player", new TextValue(context.getPlayer().getName()));
        eventData.put("world", new TextValue(context.getPlayer().getWorld().getName()));
        
        if (context.getCreativeWorld() != null) {
            eventData.put("creative_world_id", new TextValue(context.getCreativeWorld().getId()));
        }
        
        return eventData;
    }
    
    /**
     * Validates event data against event definition
     */
    private String validateEventData(CustomEvent customEvent, Map<String, DataValue> eventData) {
        for (CustomEvent.EventDataField field : customEvent.getDataFields().values()) {
            String fieldName = field.getName();
            
            if (field.isRequired() && (!eventData.containsKey(fieldName) || eventData.get(fieldName).isEmpty())) {
                return "Required field missing: " + fieldName;
            }
            
            if (eventData.containsKey(fieldName)) {
                DataValue value = eventData.get(fieldName);
                Class<?> expectedType = field.getExpectedType();
                
                // Basic type validation - check if the value is compatible
                if (expectedType != null && !field.isCompatible(value)) {
                    return "Field " + fieldName + " expects " + expectedType.getSimpleName() + 
                           " but got " + (value != null ? value.getClass().getSimpleName() : "null");
                }
            }
        }
        
        return null; // No validation errors
    }
    
    /**
     * Triggers the custom event with the provided data
     */
    private void triggerCustomEvent(CustomEventManager eventManager, CustomEvent customEvent, 
                                   Map<String, DataValue> eventData, ExecutionContext context, 
                                   boolean isGlobal, boolean isAsync) {
        
        if (isAsync) {
            // Trigger asynchronously
            context.getPlugin().getServer().getScheduler().runTaskAsynchronously(
                context.getPlugin(), 
                () -> doTriggerEvent(eventManager, customEvent, eventData, context, isGlobal)
            );
        } else {
            // Trigger synchronously
            doTriggerEvent(eventManager, customEvent, eventData, context, isGlobal);
        }
    }
    
    /**
     * Actually triggers the event
     */
    private void doTriggerEvent(CustomEventManager eventManager, CustomEvent customEvent, 
                               Map<String, DataValue> eventData, ExecutionContext context, boolean isGlobal) {
        
        // Use the actual CustomEventManager.triggerEvent method
        String worldName = context.getPlayer().getWorld().getName();
        eventManager.triggerEvent(customEvent.getName(), eventData, context.getPlayer(), worldName);
    }
    
    /**
     * Helper method to get boolean parameter with default value
     */
    private boolean getBooleanParameter(CodeBlock block, ParameterResolver resolver, 
                                      ExecutionContext context, String paramName, boolean defaultValue) {
        DataValue paramValue = block.getParameter(paramName);
        if (paramValue == null || paramValue.isEmpty()) {
            return defaultValue;
        }
        
        DataValue resolvedValue = resolver.resolve(context, paramValue);
        String valueStr = resolvedValue.asString().toLowerCase();
        
        return "true".equals(valueStr) || "yes".equals(valueStr) || "1".equals(valueStr);
    }
}