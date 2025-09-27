package com.megacreative.coding.actions.math;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.Constants;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.variables.VariableManager;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.HashMap;
import java.util.UUID;

/**
 * Math-related actions handler
 * Contains mathematical operations and calculations
 */
@BlockMeta(id = "mathActions", displayName = "§aMath Actions", type = BlockType.ACTION)
public class MathActions implements BlockAction {
    
    // Action handlers map for math actions
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
            "incrementVariable", "decrementVariable"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all math action handlers
     */
    private static void initializeActionHandlers() {
        // === MATH ACTIONS ===
        ACTION_HANDLERS.put("incrementVariable", (context, params) -> {
            String varName = params.get("variable").asString();
            double increment = params.get("amount").asNumber().doubleValue();
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current value
            DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
            double currentNumber = 0.0;
            
            if (currentValue != null && currentValue.getType().isNumber()) {
                currentNumber = currentValue.asNumber().doubleValue();
            }
            
            // Set new value
            double newValue = currentNumber + increment;
            variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
            
            context.getPlayer().sendMessage("§aVariable " + varName + " incremented to " + newValue);
        });
        
        ACTION_HANDLERS.put("decrementVariable", (context, params) -> {
            String varName = params.get("variable").asString();
            double decrement = params.get("amount").asNumber().doubleValue();
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current value
            DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
            double currentNumber = 0.0;
            
            if (currentValue != null && currentValue.getType().isNumber()) {
                currentNumber = currentValue.asNumber().doubleValue();
            }
            
            // Set new value
            double newValue = currentNumber - decrement;
            variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
            
            context.getPlayer().sendMessage("§aVariable " + varName + " decremented to " + newValue);
        });
    }
}