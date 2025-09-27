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
import java.util.function.BiConsumer;
import java.util.HashMap;

/**
 * List-related actions handler
 * Contains actions that work with lists and collections
 */
@BlockMeta(id = "listActions", displayName = "§aList Actions", type = BlockType.ACTION)
public class ListActions implements BlockAction {
    
    // Action handlers map for list actions
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
            "addItemToList", "removeItemFromList", "createList"
        );
        
        return playerRequiredActions.contains(actionType);
    }
    
    /**
     * Initialize all list action handlers
     */
    private static void initializeActionHandlers() {
        // === LIST ACTIONS ===
        ACTION_HANDLERS.put("addItemToList", (context, params) -> {
            String listName = params.get("list").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the list from player variables
            DataValue listValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), listName);
            if (listValue instanceof ListValue) {
                ListValue list = (ListValue) listValue;
                list.add(value);
                // Update the variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), listName, list);
                context.getPlayer().sendMessage("§aAdded item to list " + listName);
            } else {
                context.getPlayer().sendMessage("§cVariable " + listName + " is not a list");
            }
        });
        
        ACTION_HANDLERS.put("removeItemFromList", (context, params) -> {
            String listName = params.get("list").asString();
            DataValue value = params.get("value");
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Get the list from player variables
            DataValue listValue = variableManager.getPlayerVariable(context.getPlayer().getUniqueId(), listName);
            if (listValue instanceof ListValue) {
                ListValue list = (ListValue) listValue;
                list.remove(value);
                // Update the variable
                variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), listName, list);
                context.getPlayer().sendMessage("§aRemoved item from list " + listName);
            } else {
                context.getPlayer().sendMessage("§cVariable " + listName + " is not a list");
            }
        });
        
        ACTION_HANDLERS.put("createList", (context, params) -> {
            String listName = params.get("name").asString();
            
            // Get VariableManager from ServiceRegistry
            ServiceRegistry serviceRegistry = context.getPlugin().getServiceRegistry();
            VariableManager variableManager = serviceRegistry.getVariableManager();
            
            // Create a new empty list
            ListValue newList = new ListValue();
            
            // Store the list in player variables
            variableManager.setPlayerVariable(context.getPlayer().getUniqueId(), listName, newList);
            context.getPlayer().sendMessage("§aCreated new list " + listName);
        });
    }
}