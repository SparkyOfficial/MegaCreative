package com.megacreative.coding.actions.utils;

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
import java.util.Random;

/**
 * Utility actions handler
 * Contains miscellaneous utility actions
 */
@BlockMeta(id = "utilityActions", displayName = "§aUtility Actions", type = BlockType.ACTION)
public class UtilityActions implements BlockAction {
    
    // Action handlers map for utility actions
    private static final Map<String, BiConsumer<ExecutionContext, Map<String, DataValue>>> ACTION_HANDLERS = new HashMap<>();
    private static final Random RANDOM = new Random();
    
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
            "randomNumber", "wait", "logMessage"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all utility action handlers
     */
    private static void initializeActionHandlers() {
        // === UTILITY ACTIONS ===
        ACTION_HANDLERS.put("randomNumber", (context, params) -> {
            int min = params.get("min").asNumber().intValue();
            int max = params.get("max").asNumber().intValue();
            String varName = params.get("var").asString();
            
            // Generate random number
            int randomNum = RANDOM.nextInt(max - min + 1) + min;
            
            // Store in variable
            com.megacreative.core.ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            com.megacreative.coding.variables.VariableManager variableManager = serviceRegistry.getVariableManager();
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), varName, DataValue.of(randomNum));
            
            context.getPlayer().sendMessage("§aGenerated random number " + randomNum + " and stored in variable " + varName);
        });
        
        ACTION_HANDLERS.put("wait", (context, params) -> {
            int ticks = params.get("ticks").asNumber().intValue();
            
            // For now, we'll just send a message
            // In a full implementation, this would pause execution
            context.getPlayer().sendMessage("§aWaiting for " + ticks + " ticks (implementation placeholder)");
        });
        
        ACTION_HANDLERS.put("logMessage", (context, params) -> {
            String message = params.get("message").asString();
            String level = params.get("level").asString();
            
            // Log the message
            switch (level.toLowerCase()) {
                case "info":
                    context.getPlugin().getLogger().info(message);
                    break;
                case "warning":
                    context.getPlugin().getLogger().warning(message);
                    break;
                case "severe":
                    context.getPlugin().getLogger().severe(message);
                    break;
                default:
                    context.getPlugin().getLogger().info(message);
                    break;
            }
            
            if (context.getPlayer() != null) {
                context.getPlayer().sendMessage("§aLogged message: " + message);
            }
        });
    }
}