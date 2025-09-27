package com.megacreative.coding.actions.variables;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.Constants;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.variables.VariableManager;

import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.HashMap;

/**
 * List operations handler
 * Contains operations for working with lists
 */
@BlockMeta(id = "listOperations", displayName = "§aList Operations", type = BlockType.ACTION)
public class ListOperations implements BlockAction {
    
    // Action handlers map for list operations
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
            "addToList", "removeFromList", "createList"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all list operation handlers
     */
    private static void initializeActionHandlers() {
        // === LIST OPERATIONS ===
        ACTION_HANDLERS.put("createList", (context, params) -> {
            String listName = params.get("name").asString();
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Create empty list
            List<DataValue> emptyList = new ArrayList<>();
            variableManager.setPlayerVariable(playerId, listName, DataValue.of(emptyList));
            
            context.getPlayer().sendMessage("§aCreated list: " + listName);
        });
        
        ACTION_HANDLERS.put("addToList", (context, params) -> {
            String listName = params.get("list").asString();
            DataValue value = params.get("value");
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current list
            DataValue currentListValue = variableManager.getPlayerVariable(playerId, listName);
            List<DataValue> list = new ArrayList<>();
            
            if (currentListValue != null) {
                // Try to convert to list
                Object rawValue = currentListValue.getValue();
                if (rawValue instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<DataValue> existingList = (List<DataValue>) rawValue;
                    list.addAll(existingList);
                }
            }
            
            // Add new value
            list.add(value);
            variableManager.setPlayerVariable(playerId, listName, DataValue.of(list));
            
            context.getPlayer().sendMessage("§aAdded item to list: " + listName);
        });
        
        ACTION_HANDLERS.put("removeFromList", (context, params) -> {
            String listName = params.get("list").asString();
            DataValue value = params.get("value");
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            UUID playerId = context.getPlayer().getUniqueId();
            
            // Get current list
            DataValue currentListValue = variableManager.getPlayerVariable(playerId, listName);
            
            if (currentListValue != null) {
                // Try to convert to list
                Object rawValue = currentListValue.getValue();
                if (rawValue instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<DataValue> list = (List<DataValue>) rawValue;
                    list.remove(value);
                    variableManager.setPlayerVariable(playerId, listName, DataValue.of(list));
                    context.getPlayer().sendMessage("§aRemoved item from list: " + listName);
                }
            }
        });
    }
}