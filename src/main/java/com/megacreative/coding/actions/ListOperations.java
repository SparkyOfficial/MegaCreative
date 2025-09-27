package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.ValueType;
import com.megacreative.coding.values.types.ListValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.UUID;

/**
 * Operations for manipulating lists
 * Includes sorting, filtering, and advanced list operations
 */
@BlockMeta(id = "listOperations", displayName = "§dList Operations", type = BlockType.ACTION)
public class ListOperations implements BlockAction {
    
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
            
            switch (operation.toLowerCase()) {
                case "sort":
                    return sortList(block, context, resolver, variableManager, player);
                    
                case "reverse":
                    return reverseList(block, context, resolver, variableManager, player);
                    
                case "shuffle":
                    return shuffleList(block, context, resolver, variableManager, player);
                    
                case "filter":
                    return filterList(block, context, resolver, variableManager, player);
                    
                case "join":
                    return joinList(block, context, resolver, variableManager, player);
                    
                case "find":
                    return findInList(block, context, resolver, variableManager, player);
                    
                case "contains":
                    return containsInList(block, context, resolver, variableManager, player);
                    
                default:
                    return ExecutionResult.error("Unknown list operation: " + operation);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error in list operation: " + e.getMessage());
        }
    }
    
    /**
     * Sorts a list
     */
    private ExecutionResult sortList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                   VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            if (listNameValue == null) {
                return ExecutionResult.error("List name parameter is required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Sort the list
            java.util.List sortedList = new java.util.ArrayList();
            sortedList.addAll(list.getValues());
            Collections.sort(sortedList, (a, b) -> {
                try {
                    // Cast to DataValue first
                    DataValue dataA = (DataValue) a;
                    DataValue dataB = (DataValue) b;
                    
                    // Try to compare as numbers first
                    Double numA = dataA.asNumber().doubleValue();
                    Double numB = dataB.asNumber().doubleValue();
                    return numA.compareTo(numB);
                } catch (Exception e) {
                    // Fall back to string comparison
                    DataValue dataA = (DataValue) a;
                    DataValue dataB = (DataValue) b;
                    return dataA.asString().compareTo(dataB.asString());
                }
            });
            
            // Save the sorted list
            ListValue sortedListValue = new ListValue(sortedList);
            variableManager.setPlayerVariable(player.getUniqueId(), listName, sortedListValue);
            
            return ExecutionResult.success("List '" + listName + "' sorted successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error sorting list: " + e.getMessage());
        }
    }
    
    /**
     * Reverses a list
     */
    private ExecutionResult reverseList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                      VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            if (listNameValue == null) {
                return ExecutionResult.error("List name parameter is required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Reverse the list
            java.util.List reversedList = new java.util.ArrayList();
            reversedList.addAll(list.getValues());
            Collections.reverse(reversedList);
            
            // Save the reversed list
            ListValue reversedListValue = new ListValue(reversedList);
            variableManager.setPlayerVariable(player.getUniqueId(), listName, reversedListValue);
            
            return ExecutionResult.success("List '" + listName + "' reversed successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error reversing list: " + e.getMessage());
        }
    }
    
    /**
     * Shuffles a list randomly
     */
    private ExecutionResult shuffleList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                      VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            if (listNameValue == null) {
                return ExecutionResult.error("List name parameter is required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Shuffle the list
            java.util.List shuffledList = new java.util.ArrayList();
            shuffledList.addAll(list.getValues());
            Collections.shuffle(shuffledList);
            
            // Save the shuffled list
            ListValue shuffledListValue = new ListValue(shuffledList);
            variableManager.setPlayerVariable(player.getUniqueId(), listName, shuffledListValue);
            
            return ExecutionResult.success("List '" + listName + "' shuffled successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error shuffling list: " + e.getMessage());
        }
    }
    
    /**
     * Filters a list based on a condition
     */
    private ExecutionResult filterList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                     VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            DataValue conditionValue = block.getParameter("condition");
            DataValue resultNameValue = block.getParameter("resultName");
            
            if (listNameValue == null || conditionValue == null || resultNameValue == null) {
                return ExecutionResult.error("List name, condition, and result name parameters are required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            String condition = resolver.resolve(context, conditionValue).asString();
            String resultName = resolver.resolve(context, resultNameValue).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            List<DataValue> filteredList = new ArrayList<DataValue>();
            
            // Filter the list based on condition
            // For simplicity, we'll implement basic filtering
            switch (condition.toLowerCase()) {
                case "numbers":
                    for (int i = 0; i < list.size(); i++) {
                        DataValue item = list.get(i);
                        try {
                            item.asNumber();
                            filteredList.add(item);
                        } catch (Exception e) {
                            // Not a number, skip it
                        }
                    }
                    break;
                    
                case "strings":
                    for (int i = 0; i < list.size(); i++) {
                        DataValue item = list.get(i);
                        // Check if it's a text value
                        if (item.getType() == ValueType.TEXT) {
                            filteredList.add(item);
                        }
                    }
                    break;
                    
                case "nonempty":
                    for (int i = 0; i < list.size(); i++) {
                        DataValue item = list.get(i);
                        if (!item.isEmpty()) {
                            filteredList.add(item);
                        }
                    }
                    break;
                    
                default:
                    return ExecutionResult.error("Unknown filter condition: " + condition);
            }
            
            // Save the filtered list
            ListValue filteredListValue = new ListValue(filteredList);
            variableManager.setPlayerVariable(player.getUniqueId(), resultName, filteredListValue);
            
            return ExecutionResult.success("List '" + listName + "' filtered successfully, result saved to '" + resultName + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Error filtering list: " + e.getMessage());
        }
    }
    
    /**
     * Joins list elements into a string
     */
    private ExecutionResult joinList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                   VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            DataValue separatorValue = block.getParameter("separator");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (listNameValue == null || resultVar == null) {
                return ExecutionResult.error("List name and result variable parameters are required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            String separator = separatorValue != null ? resolver.resolve(context, separatorValue).asString() : ", ";
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Join list elements
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < list.size(); i++) {
                if (i > 0) {
                    result.append(separator);
                }
                result.append(list.get(i).asString());
            }
            
            // Save the result
            context.setVariable(resultVariable, result.toString());
            
            return ExecutionResult.success("List '" + listName + "' joined successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Error joining list: " + e.getMessage());
        }
    }
    
    /**
     * Finds an element in a list
     */
    private ExecutionResult findInList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                     VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            DataValue searchValue = block.getParameter("searchValue");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (listNameValue == null || searchValue == null || resultVar == null) {
                return ExecutionResult.error("List name, search value, and result variable parameters are required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            DataValue search = resolver.resolve(context, searchValue);
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Find the element
            int index = -1;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(search)) {
                    index = i;
                    break;
                }
            }
            
            // Save the result
            context.setVariable(resultVariable, index);
            
            if (index >= 0) {
                return ExecutionResult.success("Found element at index " + index);
            } else {
                return ExecutionResult.success("Element not found in list");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error finding in list: " + e.getMessage());
        }
    }
    
    /**
     * Checks if a list contains an element
     */
    private ExecutionResult containsInList(CodeBlock block, ExecutionContext context, ParameterResolver resolver, 
                                         VariableManager variableManager, Player player) {
        try {
            DataValue listNameValue = block.getParameter("listName");
            DataValue searchValue = block.getParameter("searchValue");
            DataValue resultVar = block.getParameter("resultVariable");
            
            if (listNameValue == null || searchValue == null || resultVar == null) {
                return ExecutionResult.error("List name, search value, and result variable parameters are required");
            }
            
            String listName = resolver.resolve(context, listNameValue).asString();
            DataValue search = resolver.resolve(context, searchValue);
            String resultVariable = resolver.resolve(context, resultVar).asString();
            
            // Get existing list
            DataValue existingList = variableManager.getPlayerVariable(player.getUniqueId(), listName);
            if (!(existingList instanceof ListValue)) {
                return ExecutionResult.error("Variable '" + listName + "' is not a list");
            }
            
            ListValue list = (ListValue) existingList;
            
            // Check if list contains the element
            boolean contains = false;
            for (int i = 0; i < list.size(); i++) {
                if (list.get(i).equals(search)) {
                    contains = true;
                    break;
                }
            }
            
            // Save the result
            context.setVariable(resultVariable, contains);
            
            if (contains) {
                return ExecutionResult.success("List contains the element");
            } else {
                return ExecutionResult.success("List does not contain the element");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Error checking list contains: " + e.getMessage());
        }
    }
}