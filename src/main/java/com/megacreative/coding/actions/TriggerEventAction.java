package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that triggers a custom event with specified data
 */
public class TriggerEventAction implements BlockAction {
    
    private final CustomEventManager eventManager;
    
    public TriggerEventAction(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player available");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get event name from parameters
            DataValue rawEventName = block.getParameter("eventName");
            if (rawEventName == null) {
                return ExecutionResult.error("Event name is required");
            }
            
            String eventName = resolver.resolve(context, rawEventName).asString();
            if (eventName == null || eventName.trim().isEmpty()) {
                return ExecutionResult.error("Invalid event name");
            }
            
            // Prepare event data from parameters
            Map<String, DataValue> eventData = new HashMap<>();
            
            // Add all parameters except eventName, worldName, and global as event data
            for (Map.Entry<String, ?> entry : block.getParameters().entrySet()) {
                String paramName = entry.getKey();
                if (!"eventName".equals(paramName) && !"worldName".equals(paramName) && !"global".equals(paramName)) {
                    Object paramValue = entry.getValue();
                    if (paramValue instanceof DataValue) {
                        eventData.put(paramName, resolver.resolve(context, (DataValue) paramValue));
                    } else {
                        eventData.put(paramName, DataValue.fromObject(paramValue));
                    }
                }
            }
            
            // Get world name (optional)
            String worldName = player.getWorld().getName();
            DataValue rawWorld = block.getParameter("worldName");
            if (rawWorld != null) {
                worldName = resolver.resolve(context, rawWorld).asString();
            }
            
            // Trigger the event
            eventManager.triggerEvent(eventName, eventData, player, worldName);
            
            return ExecutionResult.success("Event triggered: " + eventName);
            
        } catch (Exception e) {
            return ExecutionResult.error("Failed to trigger event: " + e.getMessage());
        }
    }
}