package com.megacreative.coding.actions.entity;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.Constants;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;

/**
 * Entity-related actions handler
 * Contains actions that work with entities and mobs
 */
@BlockMeta(id = "entityActions", displayName = "§aEntity Actions", type = BlockType.ACTION)
public class EntityActions implements BlockAction {
    
    // Action handlers map for entity actions
    private static final Map<String, BiConsumer<ExecutionContext, Map<String, DataValue>>> ACTION_HANDLERS = new HashMap<>();
    
    static {
        initializeActionHandlers();
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            String actionType = block.getAction();
            if (actionType == null) {
                return ExecutionResult.error(Constants.UNKNOWN_ACTION_TYPE + "null");
            }
            
            // Check if player is required for this action
            if (requiresPlayer(actionType) && context.getPlayer() == null) {
                return ExecutionResult.error(Constants.PLAYER_REQUIRED_FOR_ACTION + actionType);
            }
            
            // Get the action handler
            BiConsumer<ExecutionContext, Map<String, DataValue>> handler = ACTION_HANDLERS.get(actionType);
            if (handler != null) {
                // Execute the handler
                handler.accept(context, block.getParameters());
                return ExecutionResult.success(Constants.ACTION_EXECUTED_SUCCESSFULLY + actionType);
            } else {
                return ExecutionResult.error(Constants.UNKNOWN_ACTION_TYPE + actionType);
            }
        } catch (Exception e) {
            return ExecutionResult.error(Constants.FAILED_TO_EXECUTE_ACTION + e.getMessage());
        }
    }
    
    /**
     * Checks if a player is required for the specified action type
     */
    private boolean requiresPlayer(String actionType) {
        // Actions that require a player
        java.util.Set<String> playerRequiredActions = java.util.Set.of(
            "spawnEntity", "spawnMob", "lightning"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all entity action handlers
     */
    private static void initializeActionHandlers() {
        // === ENTITY ACTIONS ===
        ACTION_HANDLERS.put("spawnEntity", (context, params) -> {
            String entityType = params.get("type").asString();
            int count = params.get("count").asNumber().intValue();
            
            // For now, we'll just send a message
            // In a full implementation, this would spawn the entity
            context.getPlayer().sendMessage("§aSpawning " + count + " " + entityType + "(s) (implementation placeholder)");
        });
        
        ACTION_HANDLERS.put("spawnMob", (context, params) -> {
            String mobType = params.get("mob").asString();
            int amount = params.get("amount").asNumber().intValue();
            
            // For now, we'll just send a message
            // In a full implementation, this would spawn the mob
            context.getPlayer().sendMessage("§aSpawning " + amount + " " + mobType + "(s) (implementation placeholder)");
        });
        
        ACTION_HANDLERS.put("lightning", (context, params) -> {
            // Strike lightning at player's location
            context.getPlayer().getLocation().getWorld().strikeLightning(context.getPlayer().getLocation());
            context.getPlayer().sendMessage("§aLightning struck at your location");
        });
    }
}