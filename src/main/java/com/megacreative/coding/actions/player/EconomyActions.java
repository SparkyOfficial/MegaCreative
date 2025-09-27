package com.megacreative.coding.actions.player;

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
 * Economy-related actions handler
 * Contains actions that affect player economy (placeholder implementation)
 */
@BlockMeta(id = "economyActions", displayName = "§aEconomy Actions", type = BlockType.ACTION)
public class EconomyActions implements BlockAction {
    
    // Action handlers map for economy actions
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
            "giveMoney", "takeMoney"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all economy action handlers
     */
    private static void initializeActionHandlers() {
        // === ECONOMY ACTIONS (placeholder implementation) ===
        ACTION_HANDLERS.put("giveMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Placeholder implementation - in a full implementation, this would integrate with an economy plugin
            context.getPlayer().sendMessage("§a+$" + amount + " (Economy integration placeholder - not implemented)");
        });
        
        ACTION_HANDLERS.put("takeMoney", (context, params) -> {
            double amount = params.get("amount").asNumber().doubleValue();
            // Placeholder implementation - in a full implementation, this would integrate with an economy plugin
            context.getPlayer().sendMessage("§c-$" + amount + " (Economy integration placeholder - not implemented)");
        });
    }
}