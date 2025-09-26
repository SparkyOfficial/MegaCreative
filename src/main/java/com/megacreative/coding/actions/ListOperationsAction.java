package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Action that performs various operations on lists
 */
public class ListOperationsAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        
        if (player == null || variableManager == null) {
            return ExecutionResult.error("No player or variable manager available");
        }
        
        ParameterResolver resolver = new ParameterResolver(context);
        
        try {
            // Get operation type
            DataValue operationValue = block.getParameter("operation");
            if (operationValue == null) {
                return ExecutionResult.error("Operation parameter is required");
            }
            
            String operation = resolver.resolve(context, operationValue).asString();
            
            // Get list name
            DataValue listNameValue = block.getParameter("listName");
            if (listNameValue == null) {
                return ExecutionResult.error("List name parameter is required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            
            switch (operation.toLowerCase()) {
                case "add":
                    return addToList(block, context, resolver, listName, variableManager, player);
                    
                case "remove":
                    return removeFromList(block, context, resolver, listName, variableManager, player);
                    
                case "get":
                    return getFromList(block, context, resolver, listName, variableManager, player);
                    
                case "size":
                    return getListSize(block, context, resolver, listName, variableManager, player);
                    
                case "clear":
                    return clearList(block, context, resolver, listName, variableManager, player);
                    
                case "createfromstring":
                    return createListFromString(block, context, resolver, listName, variableManager, player);
                    
                default:
                    return ExecutionResult.error("Unknown list operation: " + operation);
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Error in list operation: " + e.getMessage());
        }
    }
    
    private ExecutionResult addToList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                     String listName, VariableManager variableManager, Player player) {
        try {
            DataValue valueToAdd = block.getParameter("value");
            if (valueToAdd == null) {
                return ExecutionResult.error("Value parameter is required for add operation");
            }
            
            DataValue resolvedValue = resolver.resolve(context, valueToAdd);
            
            // Get existing list or create new one
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            ListValue list;
            
            if (existingList instanceof ListValue) {
                list = (ListValue) existingList;
            } else {
                list = new ListValue(new ArrayList<>());
            }
            
            // Add the value to the list
            list.add(resolvedValue);
            
            // Save the updated list
            variableManager.setPlayerVariable(player.getUniqueId(), listName, list);
            
            return ExecutionResult.success("Added value to list '" + listName + "'");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error adding to list: " + e.getMessage());
        }
    }
    
    private ExecutionResult removeFromList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                          String listName, VariableManager variableManager, Player player) {
        try {
            DataValue valueToRemove = block.getParameter("value");
            if (valueToRemove == null) {
                return ExecutionResult.error("Value parameter is required for remove operation");
            }
            
            DataValue resolvedValue = resolver.resolve(context, valueToRemove);
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Remove the value from the list
            boolean removed = list.remove(resolvedValue);
            
            // Save the updated list
            variableManager.setPlayerVariable(player.getUniqueId(), listName, list);
            
            if (removed) {
                return ExecutionResult.success("Removed value from list '" + listName + "'");
            } else {
                return ExecutionResult.success("Value not found in list '" + listName + "'");
            }
            
        } catch (Exception e) {
            return ExecutionResult.error("Error removing from list: " + e.getMessage());
        }
    }
    
    private ExecutionResult getFromList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                       String listName, VariableManager variableManager, Player player) {
        try {
            DataValue indexValue = block.getParameter("index");
            if (indexValue == null) {
                return ExecutionResult.error("Index parameter is required for get operation");
            }
            
            int index = resolver.resolve(context, indexValue).asNumber().intValue();
            
            // Get target variable name
            DataValue targetVariableValue = block.getParameter("targetVariable");
            if (targetVariableValue == null) {
                return ExecutionResult.error("Target variable parameter is required for get operation");
            }
            
            String targetVariable = resolver.resolve(context, targetVariableValue).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Get the value from the list
            if (index < 0 || index >= list.size()) {
                return ExecutionResult.error("Index out of bounds: " + index);
            }
            
            DataValue value = list.get(index);
            
            // Save the value to target variable
            variableManager.setPlayerVariable(player.getUniqueId(), targetVariable, value);
            
            return ExecutionResult.success("Retrieved value from list '" + listName + "' at index " + index);
            
        } catch (Exception e) {
            return ExecutionResult.error("Error getting from list: " + e.getMessage());
        }
    }
    
    private ExecutionResult getListSize(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                       String listName, VariableManager variableManager, Player player) {
        try {
            // Get target variable name
            DataValue targetVariableValue = block.getParameter("targetVariable");
            if (targetVariableValue == null) {
                return ExecutionResult.error("Target variable parameter is required for size operation");
            }
            
            String targetVariable = resolver.resolve(context, targetVariableValue).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            int size = 0;
            
            if (existingList instanceof ListValue) {
                size = ((ListValue) existingList).size();
            }
            
            // Save the size to target variable
            variableManager.setPlayerVariable(player.getUniqueId(), targetVariable, DataValue.fromObject(size));
            
            return ExecutionResult.success("Size of list '" + listName + "' is " + size);
            
        } catch (Exception e) {
            return ExecutionResult.error("Error getting list size: " + e.getMessage());
        }
    }
    
    private ExecutionResult clearList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                     String listName, VariableManager variableManager, Player player) {
        try {
            // Clear the list
            ListValue newList = new ListValue(new ArrayList<>());
            
            // Save the empty list
            variableManager.setPlayerVariable(player.getUniqueId(), listName, newList);
            
            return ExecutionResult.success("Cleared list '" + listName + "'");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error clearing list: " + e.getMessage());
        }
    }
    
    private ExecutionResult createListFromString(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                                String listName, VariableManager variableManager, Player player) {
        try {
            DataValue listStringValue = block.getParameter("listString");
            if (listStringValue == null) {
                return ExecutionResult.error("List string parameter is required for createFromString operation");
            }
            
            String listString = resolver.resolve(context, listStringValue).asString();
            
            // Parse the list string (this would use the ActionFactory's parseListString method in a full implementation)
            ListValue list = parseListString(listString);
            
            // Save the list
            variableManager.setPlayerVariable(player.getUniqueId(), listName, list);
            
            return ExecutionResult.success("Created list '" + listName + "' from string");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error creating list from string: " + e.getMessage());
        }
    }
    
    /**
     * Parses a string representation of a list into a ListValue
     * Supports formats like "[item1,item2,item3]" or "item1,item2,item3"
     */
    private ListValue parseListString(String listString) {
        if (listString == null || listString.trim().isEmpty()) {
            return new ListValue(new ArrayList<>());
        }
        
        // Remove brackets if present
        String cleanString = listString.trim();
        if (cleanString.startsWith("[")) {
            cleanString = cleanString.substring(1);
        }
        if (cleanString.endsWith("]")) {
            cleanString = cleanString.substring(0, cleanString.length() - 1);
        }
        
        // Split by comma and create DataValues
        List<DataValue> values = new ArrayList<>();
        if (!cleanString.isEmpty()) {
            String[] items = cleanString.split(",");
            for (String item : items) {
                String trimmedItem = item.trim();
                // Try to parse as number first
                try {
                    double number = Double.parseDouble(trimmedItem);
                    values.add(DataValue.fromObject(number));
                } catch (NumberFormatException e) {
                    // Treat as string
                    values.add(DataValue.fromObject(trimmedItem));
                }
            }
        }
        
        return new ListValue(values);
    }
}