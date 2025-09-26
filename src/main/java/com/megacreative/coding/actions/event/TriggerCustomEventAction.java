package com.megacreative.coding.actions.event;

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
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

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
@BlockMeta(id = "triggerCustomEvent", displayName = "§aTrigger Custom Event", type = BlockType.ACTION)
public class TriggerCustomEventAction implements BlockAction {
    
    private static final String EVENT_NAME_PARAM = "event_name";
    private static final String GLOBAL_PARAM = "global";
    private static final String ASYNC_PARAM = "async";
    
    @Override
    public com.megacreative.coding.executors.ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            validateAndTriggerEvent(block, context);
            return com.megacreative.coding.executors.ExecutionResult.success("Custom event triggered");
        } catch (Exception e) {
            handleExecutionError(context, e);
            return com.megacreative.coding.executors.ExecutionResult.error("Failed to trigger custom event: " + e.getMessage());
        }
    }
    
    private void validateAndTriggerEvent(CodeBlock block, ExecutionContext context) {
        Player player = validateContext(context);
        if (player == null) return;
        
        if (block == null) return;
        
        String eventName = resolveEventName(block, new ParameterResolver(context), context);
        if (eventName == null) {
            player.sendMessage("§cEvent name is required");
            return;
        }
        
        CustomEventManager eventManager = getEventManager(context);
        if (eventManager == null) {
            player.sendMessage("§cCustom Event Manager is not available");
            return;
        }
        
        CustomEvent customEvent = eventManager.getEventByName(eventName);
        if (customEvent == null) {
            // Create a new event if it doesn't exist
            customEvent = new CustomEvent(eventName, "Dynamically created event: " + eventName);
            eventManager.registerEvent(customEvent);
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        Map<String, DataValue> eventData = collectEventData(context, block, resolver, customEvent);
        if (!validateEventData(customEvent, eventData)) {
            player.sendMessage("§cInvalid event data for event: " + eventName);
            return;
        }
        
        boolean isGlobal = getBooleanParameter(block, resolver, context, GLOBAL_PARAM, false);
        boolean isAsync = getBooleanParameter(block, resolver, context, ASYNC_PARAM, false);
        
        triggerCustomEvent(eventManager, customEvent, eventData, context, isGlobal, isAsync);
    }
    
    private Player validateContext(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return null;
        }
        
        if (context.getCurrentBlock() == null) {
            return null;
        }
        
        return player;
    }
    
    private String resolveEventName(CodeBlock block, ParameterResolver resolver, ExecutionContext context) {
        DataValue eventNameValue = block.getParameter(EVENT_NAME_PARAM);
        if (eventNameValue == null) {
            return null;
        }
        
        DataValue resolvedName = resolver.resolve(context, eventNameValue);
        if (resolvedName == null) {
            return null;
        }
        
        String name = resolvedName.asString();
        return (name == null || name.trim().isEmpty()) ? null : name.trim();
    }
    
    private CustomEventManager getEventManager(ExecutionContext context) {
        return context.getPlugin().getServiceRegistry().getCustomEventManager();
    }
    
    private void handleExecutionError(ExecutionContext context, Exception e) {
        Player player = context.getPlayer();
        String errorMsg = "Error triggering custom event: " + e.getMessage();
        
        if (player != null) {
            player.sendMessage("§c" + errorMsg);
        }
        
        if (context.isDebugMode()) {
            context.getPlugin().getLogger().log(Level.SEVERE, errorMsg, e);
        }
    }
    
    private Map<String, DataValue> collectEventData(ExecutionContext context, CodeBlock block, 
                                                   ParameterResolver resolver, CustomEvent customEvent) {
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
    
    private boolean validateEventData(CustomEvent customEvent, Map<String, DataValue> eventData) {
        for (CustomEvent.EventDataField field : customEvent.getDataFields().values()) {
            String fieldName = field.getName();
            
            if (field.isRequired() && (!eventData.containsKey(fieldName) || eventData.get(fieldName).isEmpty())) {
                return false;
            }
            
            if (eventData.containsKey(fieldName)) {
                DataValue value = eventData.get(fieldName);
                Class<?> expectedType = field.getExpectedType();
                
                // Basic type validation - check if the value is compatible
                if (expectedType != null && !field.isCompatible(value)) {
                    return false;
                }
            }
        }
        
        return true; // No validation errors
    }
    
    private void triggerCustomEvent(CustomEventManager eventManager, CustomEvent customEvent, 
                                   Map<String, DataValue> eventData, ExecutionContext context, 
                                   boolean isGlobal, boolean isAsync) {
        if (isAsync) {
            // Run in a separate thread if async is true
            new Thread(() -> doTriggerEvent(eventManager, customEvent, eventData, context, isGlobal), 
                     "EventTrigger-" + customEvent.getName()).start();
        } else {
            doTriggerEvent(eventManager, customEvent, eventData, context, isGlobal);
        }
    }
    
    private void doTriggerEvent(CustomEventManager eventManager, CustomEvent customEvent, 
                               Map<String, DataValue> eventData, ExecutionContext context, boolean isGlobal) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        String worldName = isGlobal ? null : player.getWorld().getName();
        eventManager.triggerEvent(customEvent.getName(), eventData, isGlobal ? null : player, worldName);
    }
    
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