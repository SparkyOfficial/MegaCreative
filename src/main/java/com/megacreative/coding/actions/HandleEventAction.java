package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.events.CustomEventManager;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Action that registers a code block as an event handler
 */
@BlockMeta(id = "handleEvent", displayName = "§aHandle Event", type = BlockType.ACTION)
public class HandleEventAction implements BlockAction {
    
    private final CustomEventManager eventManager;
    
    public HandleEventAction(CustomEventManager eventManager) {
        this.eventManager = eventManager;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        
        if (player == null || block == null || variableManager == null) {
            return ExecutionResult.error("Player, block, or variable manager is null");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get event name from parameters
            DataValue rawEventName = block.getParameter("eventName");
            if (rawEventName == null) {
                player.sendMessage("§cEvent name is required");
                return ExecutionResult.error("Event name is required");
            }
            
            String eventName = resolver.resolve(context, rawEventName).asString();
            if (eventName == null || eventName.trim().isEmpty()) {
                player.sendMessage("§cInvalid event name");
                return ExecutionResult.error("Invalid event name");
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
            
            return ExecutionResult.success("Event handler registered");
        } catch (Exception e) {
            player.sendMessage("§c✗ Failed to register event handler: " + e.getMessage());
            return ExecutionResult.error("Failed to register event handler: " + e.getMessage());
        }
    }
}