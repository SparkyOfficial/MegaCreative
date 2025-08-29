package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Action that triggers a custom event with data
 */
public class TriggerEventAction implements BlockAction {
    
    private final CustomEventManager eventManager;
    
    public TriggerEventAction(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        try {
            // Get event name from parameters
            DataValue rawEventName = block.getParameter("eventName");
            if (rawEventName == null) {
                player.sendMessage("§cEvent name is required");
                return;
            }
            
            String eventName = resolver.resolve(context, rawEventName).asString();
            if (eventName == null || eventName.trim().isEmpty()) {
                player.sendMessage("§cInvalid event name");
                return;
            }
            
            // Collect event data from parameters that start with "data_"
            Map<String, DataValue> eventData = new HashMap<>();
            
            // Get all block parameters
            Map<String, DataValue> allParams = block.getParameters();
            if (allParams != null) {
                for (Map.Entry<String, DataValue> entry : allParams.entrySet()) {
                    String paramName = entry.getKey();
                    if (paramName.startsWith("data_")) {
                        String dataFieldName = paramName.substring(5); // Remove "data_" prefix
                        DataValue resolvedValue = resolver.resolve(context, entry.getValue());
                        eventData.put(dataFieldName, resolvedValue);
                    }
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
}