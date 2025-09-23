package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEvent;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.executors.ExecutionResult;
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
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null) {
            return ExecutionResult.error("CodeBlock cannot be null");
        }
        if (context == null) {
            return ExecutionResult.error("ExecutionContext cannot be null");
        }
        
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player available");
        }
        
        CodeBlock currentBlock = context.getCurrentBlock();
        if (currentBlock == null) {
            return ExecutionResult.error("Current block is not available");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get event name from parameter
            String eventName = null;
            DataValue eventNameValue = block.getParameter("event_name");
            
            if (eventNameValue != null && !eventNameValue.isEmpty()) {
                DataValue resolvedName = resolver.resolve(context, eventNameValue);
                eventName = resolvedName.asString();
            } else {
                // Fallback to GUI slot
                // Get slot resolver from BlockConfigService
                com.megacreative.services.BlockConfigService configService = 
                    context.getPlugin().getServiceRegistry().getBlockConfigService();
                java.util.function.Function<String, Integer> slotResolver = 
                    configService != null ? configService.getSlotResolver("triggerCustomEvent") : null;
                    
                var nameItem = slotResolver != null ? 
                    block.getItemFromSlot("event_name_slot", slotResolver) : null;
                if (nameItem != null && nameItem.hasItemMeta()) {
                    eventName = nameItem.getItemMeta().getDisplayName();
                }
            }
            
            if (eventName == null || eventName.trim().isEmpty()) {
                player.sendMessage("§c[TriggerEvent] No event name specified!");
                return ExecutionResult.error("No event name specified");
            }
            
            eventName = eventName.trim();
            
            // Get CustomEventManager
            CustomEventManager eventManager = context.getPlugin().getServiceRegistry().getCustomEventManager();
            if (eventManager == null) {
                player.sendMessage("§c[TriggerEvent] Custom event system not available!");
                return ExecutionResult.error("Custom event system not available");
            }
            
            // Check if event exists
            Map<String, CustomEvent> events = eventManager.getEvents();
            CustomEvent customEvent = events.get(eventName);
            if (customEvent == null) {
                player.sendMessage("§c[TriggerEvent] Custom event not found: " + eventName);
                return ExecutionResult.error("Custom event not found: " + eventName);
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
                return ExecutionResult.error("Invalid event data: " + validationError);
            }
            
            // Trigger the custom event
            triggerCustomEvent(eventManager, customEvent, eventData, context, isGlobal, isAsync);
            
            player.sendMessage("§a[TriggerEvent] Triggered event: " + eventName + 
                              (isGlobal ? " (global)" : " (world)") + 
                              (isAsync ? " (async)" : ""));
            
            return ExecutionResult.success();
        } catch (Exception e) {
            player.sendMessage("§c[TriggerEvent] Error triggering event: " + e.getMessage());
            context.getPlugin().getLogger().warning("TriggerEvent execution error: " + e.getMessage());
            return ExecutionResult.error("Error triggering event: " + e.getMessage());
        }
    }
    
    /**
     * Collects event data from block parameters and variables
     */
    private Map<String, DataValue> collectEventData(ExecutionContext context, CodeBlock block, 
                                                   ParameterResolver resolver, CustomEvent customEvent) {
        if (context == null || block == null || resolver == null || customEvent == null) {
            throw new IllegalArgumentException("Parameters cannot be null");
        }
        
        Map<String, DataValue> eventData = new HashMap<>();
        
        // Try to get data from "data" parameter (could be a variable reference to a map)
        DataValue dataValue = block.getParameter("data");
        if (dataValue != null && !dataValue.isEmpty()) {
            DataValue resolvedData = resolver.resolve(context, dataValue);
            // Implement proper JSON parsing and variable reference resolution
            // Detect if the data is a JSON string and parse it into structured data
            // Also resolve variable references in the data
            try {
                String dataStr = resolvedData.asString();
                if (dataStr != null && isJsonString(dataStr)) {
                    // Parse JSON string into a map
                    Map<String, Object> jsonData = parseJsonString(dataStr, context);
                    // Convert to DataValue map
                    for (Map.Entry<String, Object> entry : jsonData.entrySet()) {
                        eventData.put(entry.getKey(), DataValue.fromObject(entry.getValue()));
                    }
                } else {
                    // Handle as variable reference or simple string
                    eventData.put("data", resolvedData);
                }
            } catch (Exception e) {
                // If parsing fails, handle as simple string
                eventData.put("data", resolvedData);
                context.getPlugin().getLogger().warning("Failed to parse data parameter as JSON: " + e.getMessage());
            }
        }
        
        // Collect data for each field defined in the custom event
        for (CustomEvent.EventDataField field : customEvent.getDataFields().values()) {
            if (field == null) {
                continue; // Skip null fields
            }
            
            String fieldName = field.getName();
            if (fieldName == null) {
                continue; // Skip fields with null names
            }
            
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
        if (customEvent == null || eventData == null) {
            return "CustomEvent and eventData cannot be null";
        }
        
        for (CustomEvent.EventDataField field : customEvent.getDataFields().values()) {
            if (field == null) {
                continue; // Skip null fields
            }
            
            String fieldName = field.getName();
            if (fieldName == null) {
                continue; // Skip fields with null names
            }
            
            if (field.isRequired() && (!eventData.containsKey(fieldName) || eventData.get(fieldName) == null || eventData.get(fieldName).isEmpty())) {
                return "Required field missing: " + fieldName;
            }
            
            if (eventData.containsKey(fieldName)) {
                DataValue value = eventData.get(fieldName);
                if (value == null) {
                    continue; // Skip null values
                }
                
                Class<?> expectedType = field.getExpectedType();
                if (expectedType == null) {
                    continue; // Skip validation if expected type is not specified
                }
                
                // Basic type validation - check if the value is compatible
                if (!field.isCompatible(value)) {
                    return "Field " + fieldName + " expects " + expectedType.getSimpleName() + 
                           " but got " + value.getClass().getSimpleName();
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
    
    /**
     * Checks if a string appears to be JSON
     */
    private boolean isJsonString(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        str = str.trim();
        return (str.startsWith("{") && str.endsWith("}")) || (str.startsWith("[") && str.endsWith("]"));
    }
    
    /**
     * Parses a JSON string into a Map
     */
    private Map<String, Object> parseJsonString(String jsonStr, ExecutionContext context) {
        Map<String, Object> result = new HashMap<>();
        try {
            // Use Gson for proper JSON parsing instead of the simple implementation
            com.google.gson.Gson gson = new com.google.gson.Gson();
            // Parse as a generic map
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<Map<String, Object>>(){}.getType();
            result = gson.fromJson(jsonStr, type);
            
            // Handle null result
            if (result == null) {
                result = new HashMap<>();
            }
        } catch (Exception e) {
            // Return empty map if parsing fails
            if (context.getPlugin() != null) {
                context.getPlugin().getLogger().warning("Failed to parse JSON string: " + e.getMessage());
            }
        }
        return result;
    }
}