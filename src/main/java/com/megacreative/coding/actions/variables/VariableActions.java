package com.megacreative.coding.actions.variables;

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
 * Variable-related actions handler
 * Contains actions that work with variables
 */
@BlockMeta(id = "variableActions", displayName = "§aVariable Actions", type = BlockType.ACTION)
public class VariableActions implements BlockAction {
    
    // Action handlers map for variable actions
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
            "setVar", "getVar", "addVar", "subVar", "mulVar", "divVar",
            "setGlobalVar", "getGlobalVar", "setServerVar", "getServerVar"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all variable action handlers
     */
    private static void initializeActionHandlers() {
        // === BASIC VARIABLE ACTIONS ===
        ACTION_HANDLERS.put("setVar", (context, params) -> {
            String varName = params.get("name").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Set the variable
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), varName, value);
            context.getPlayer().sendMessage("§aSet variable " + varName + " to " + value.asString());
        });
        
        ACTION_HANDLERS.put("getVar", (context, params) -> {
            String varName = params.get("name").asString();
            String targetVar = params.get("target").asString();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the variable
            DataValue value = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), varName);
            if (value != null) {
                // Store in target variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), targetVar, value);
                context.getPlayer().sendMessage("§aRetrieved variable " + varName + " into " + targetVar);
            } else {
                context.getPlayer().sendMessage("§cVariable " + varName + " not found");
            }
        });
        
        ACTION_HANDLERS.put("addVar", (context, params) -> {
            String varName = params.get("name").asString();
            double addValue = params.get("value").asNumber().doubleValue();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current value
            DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
            double currentNumber = 0.0;
            
            if (currentValue != null && currentValue.getType().isNumber()) {
                currentNumber = currentValue.asNumber().doubleValue();
            }
            
            // Set new value
            double newValue = currentNumber + addValue;
            variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
            
            context.getPlayer().sendMessage("§aAdded " + addValue + " to variable " + varName + " (now " + newValue + ")");
        });
        
        ACTION_HANDLERS.put("subVar", (context, params) -> {
            String varName = params.get("name").asString();
            double subValue = params.get("value").asNumber().doubleValue();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current value
            DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
            double currentNumber = 0.0;
            
            if (currentValue != null && currentValue.getType().isNumber()) {
                currentNumber = currentValue.asNumber().doubleValue();
            }
            
            // Set new value
            double newValue = currentNumber - subValue;
            variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
            
            context.getPlayer().sendMessage("§aSubtracted " + subValue + " from variable " + varName + " (now " + newValue + ")");
        });
        
        ACTION_HANDLERS.put("mulVar", (context, params) -> {
            String varName = params.get("name").asString();
            double mulValue = params.get("value").asNumber().doubleValue();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current value
            DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
            double currentNumber = 0.0;
            
            if (currentValue != null && currentValue.getType().isNumber()) {
                currentNumber = currentValue.asNumber().doubleValue();
            }
            
            // Set new value
            double newValue = currentNumber * mulValue;
            variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
            
            context.getPlayer().sendMessage("§aMultiplied variable " + varName + " by " + mulValue + " (now " + newValue + ")");
        });
        
        ACTION_HANDLERS.put("divVar", (context, params) -> {
            String varName = params.get("name").asString();
            double divValue = params.get("value").asNumber().doubleValue();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current value
            DataValue currentValue = variableManager.getPlayerVariable(playerId, varName);
            double currentNumber = 0.0;
            
            if (currentValue != null && currentValue.getType().isNumber()) {
                currentNumber = currentValue.asNumber().doubleValue();
            }
            
            // Check for division by zero
            if (divValue == 0) {
                context.getPlayer().sendMessage("§cCannot divide by zero");
                return;
            }
            
            // Set new value
            double newValue = currentNumber / divValue;
            variableManager.setPlayerVariable(playerId, varName, DataValue.of(newValue));
            
            context.getPlayer().sendMessage("§aDivided variable " + varName + " by " + divValue + " (now " + newValue + ")");
        });
        
        // === GLOBAL VARIABLE ACTIONS ===
        ACTION_HANDLERS.put("setGlobalVar", (context, params) -> {
            String varName = params.get("name").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Set the global variable
            variableManager.setGlobalVariable(varName, value);
            context.getPlayer().sendMessage("§aSet global variable " + varName + " to " + value.asString());
        });
        
        ACTION_HANDLERS.put("getGlobalVar", (context, params) -> {
            String varName = params.get("name").asString();
            String targetVar = params.get("target").asString();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the global variable
            DataValue value = variableManager.getGlobalVariable(varName);
            if (value != null) {
                // Store in target variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), targetVar, value);
                context.getPlayer().sendMessage("§aRetrieved global variable " + varName + " into " + targetVar);
            } else {
                context.getPlayer().sendMessage("§cGlobal variable " + varName + " not found");
            }
        });
        
        // === SERVER VARIABLE ACTIONS ===
        ACTION_HANDLERS.put("setServerVar", (context, params) -> {
            String varName = params.get("name").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Set the server variable
            variableManager.setServerVariable(varName, value);
            context.getPlayer().sendMessage("§aSet server variable " + varName + " to " + value.asString());
        });
        
        ACTION_HANDLERS.put("getServerVar", (context, params) -> {
            String varName = params.get("name").asString();
            String targetVar = params.get("target").asString();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the server variable
            DataValue value = variableManager.getServerVariable(varName);
            if (value != null) {
                // Store in target variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), targetVar, value);
                context.getPlayer().sendMessage("§aRetrieved server variable " + varName + " into " + targetVar);
            } else {
                context.getPlayer().sendMessage("§cServer variable " + varName + " not found");
            }
        });
    }
}