package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

import java.util.Map;

/**
 * Action that registers a code block as an event handler
 */
public class HandleEventAction extends CodingAction {
    
    private final CustomEventManager eventManager;
    
    public HandleEventAction(CustomEventManager eventManager) {
        super("handleEvent", "Handle Event", 
              "Registers this code block to handle a specific event");
        this.eventManager = eventManager;
    }
    
    @Override
    public void execute(Player player, Map<String, DataValue> parameters) {
        try {
            // Get event name from parameters
            DataValue eventNameValue = parameters.get("eventName");
            if (eventNameValue == null) {
                throw new IllegalArgumentException("Event name is required");
            }
            
            String eventName = eventNameValue.asString();
            
            // Get priority (optional, default is 0)
            DataValue priorityValue = parameters.get("priority");
            int priority = priorityValue != null ? priorityValue.asNumber().intValue() : 0;
            
            // Get world restriction (optional)
            DataValue worldValue = parameters.get("worldName");
            String worldName = worldValue != null ? worldValue.asString() : null;
            
            // Get global flag (optional, default is false)
            DataValue globalValue = parameters.get("global");
            boolean isGlobal = globalValue != null && globalValue.asBoolean();
            
            // Get the code block that contains this action
            CodeBlock handlerBlock = getCurrentBlock();
            if (handlerBlock == null) {
                throw new IllegalStateException("Cannot determine current code block");
            }
            
            // Create and register event handler
            CustomEventManager.EventHandler handler = eventManager.createEventHandler(
                handlerBlock, 
                isGlobal ? null : player, 
                isGlobal ? null : (worldName != null ? worldName : player.getWorld().getName()),
                priority
            );
            
            eventManager.registerEventHandler(eventName, handler);
            
            // Send confirmation to player
            String scope = isGlobal ? "globally" : (worldName != null ? "in world " + worldName : "in current world");
            player.sendMessage("§a✓ Registered handler for event: " + eventName + " " + scope + " (priority: " + priority + ")");
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Failed to register event handler: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate(Map<String, DataValue> parameters) {
        DataValue eventNameValue = parameters.get("eventName");
        return eventNameValue != null && !eventNameValue.asString().trim().isEmpty();
    }
    
    /**
     * Gets the current code block being executed
     * This would need to be implemented in the execution context
     */
    private CodeBlock getCurrentBlock() {
        // This would be set by the execution engine when running actions
        // For now, return null - this would need integration with the execution system
        return null;
    }
    
    /**
     * Helper method to create a handle event action for specific event
     */
    public static HandleEventAction createForEvent(CustomEventManager eventManager, String eventName) {
        HandleEventAction action = new HandleEventAction(eventManager);
        action.setName("handle_" + eventName);
        action.setDisplayName("Handle " + eventName);
        action.setDescription("Registers this block to handle the '" + eventName + "' event");
        return action;
    }
}