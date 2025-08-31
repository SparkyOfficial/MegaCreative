package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Action that registers a code block as an event handler
 */
public class HandleEventAction implements BlockAction {
    
    private final CustomEventManager eventManager;
    
    public HandleEventAction(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();
        
        if (player == null || block == null || variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);
        
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
            
            // Get priority (optional, default is 0)
            DataValue rawPriority = block.getParameter("priority");
            int priority = 0;
            if (rawPriority != null) {
                try {
                    priority = resolver.resolve(context, rawPriority).asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default priority
                }
            }
            
            // Get world restriction (optional)
            DataValue rawWorld = block.getParameter("worldName");
            String worldName = null;
            if (rawWorld != null) {
                worldName = resolver.resolve(context, rawWorld).asString();
            }
            
            // Get global flag (optional, default is false)
            DataValue rawGlobal = block.getParameter("global");
            boolean isGlobal = false;
            if (rawGlobal != null) {
                String globalStr = resolver.resolve(context, rawGlobal).asString();
                isGlobal = "true".equalsIgnoreCase(globalStr) || "yes".equalsIgnoreCase(globalStr);
            }
            
            // Create and register event handler
            CustomEventManager.EventHandler handler = eventManager.createEventHandler(
                block, 
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
}