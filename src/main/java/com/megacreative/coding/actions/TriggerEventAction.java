package com.megacreative.coding.actions;

import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.TextValue;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that triggers a custom event with data
 */
public class TriggerEventAction extends CodingAction {
    
    private final CustomEventManager eventManager;
    
    public TriggerEventAction(CustomEventManager eventManager) {
        super("triggerEvent", "Trigger Event", 
              "Triggers a custom event with the specified data");
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
            
            // Collect event data from additional parameters
            Map<String, DataValue> eventData = new HashMap<>();
            
            // Look for parameters that start with "data_" to use as event data
            for (Map.Entry<String, DataValue> entry : parameters.entrySet()) {
                String paramName = entry.getKey();
                if (paramName.startsWith("data_")) {
                    String dataFieldName = paramName.substring(5); // Remove "data_" prefix
                    eventData.put(dataFieldName, entry.getValue());
                }
            }
            
            // Get world name
            String worldName = player.getWorld().getName();
            
            // Trigger the event
            eventManager.triggerEvent(eventName, eventData, player, worldName);
            
            // Send confirmation to player
            player.sendMessage("§a✓ Triggered event: " + eventName);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Failed to trigger event: " + e.getMessage());
        }
    }
    
    @Override
    public boolean validate(Map<String, DataValue> parameters) {
        DataValue eventNameValue = parameters.get("eventName");
        return eventNameValue != null && !eventNameValue.asString().trim().isEmpty();
    }
    
    /**
     * Helper method to create a trigger event action with specific event name
     */
    public static TriggerEventAction createForEvent(CustomEventManager eventManager, String eventName) {
        TriggerEventAction action = new TriggerEventAction(eventManager);
        action.setName("trigger_" + eventName);
        action.setDisplayName("Trigger " + eventName);
        action.setDescription("Triggers the '" + eventName + "' event");
        return action;
    }
}