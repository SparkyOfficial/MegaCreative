package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

import java.util.List;

/**
 * Action to perform operations on a list
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "listOperation", displayName = "§bList Operation", type = BlockType.ACTION)
public class ListOperationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue listNameValue = block.getParameter("listName");
            DataValue operationValue = block.getParameter("operation");
            DataValue valueValue = block.getParameter("value");
            DataValue indexValue = block.getParameter("index");
            
            if (listNameValue == null || operationValue == null) {
                return ExecutionResult.error("Missing required parameters: listName, operation");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedListName = resolver.resolve(context, listNameValue);
            DataValue resolvedOperation = resolver.resolve(context, operationValue);
            
            String listName = resolvedListName.asString();
            String operation = resolvedOperation.asString();
            
            // Get list variable
            Object listObj = context.getVariable(listName);
            if (!(listObj instanceof List)) {
                return ExecutionResult.error("Variable " + listName + " is not a list");
            }
            
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) listObj;
            
            // Perform operation
            switch (operation.toLowerCase()) {
                case "add":
                    if (valueValue != null) {
                        DataValue resolvedValue = resolver.resolve(context, valueValue);
                        list.add(resolvedValue.asString());
                    }
                    break;
                case "remove":
                    if (valueValue != null) {
                        DataValue resolvedValue = resolver.resolve(context, valueValue);
                        list.remove(resolvedValue.asString());
                    }
                    break;
                case "get":
                    if (indexValue != null) {
                        DataValue resolvedIndex = resolver.resolve(context, indexValue);
                        int index = resolvedIndex.asNumber().intValue();
                        if (index >= 0 && index < list.size()) {
                            Object value = list.get(index);
                            // Store value in a variable (we'd need a target variable parameter for this)
                        }
                    }
                    break;
                case "set":
                    if (indexValue != null && valueValue != null) {
                        DataValue resolvedIndex = resolver.resolve(context, indexValue);
                        DataValue resolvedValue = resolver.resolve(context, valueValue);
                        int index = resolvedIndex.asNumber().intValue();
                        if (index >= 0 && index < list.size()) {
                            list.set(index, resolvedValue.asString());
                        }
                    }
                    break;
                case "size":
                    // Return size (we'd need a target variable parameter for this)
                    break;
                default:
                    return ExecutionResult.error("Invalid operation: " + operation);
            }
            
            // Update list variable
            context.setVariable(listName, list);
            
            return ExecutionResult.success("Performed " + operation + " operation on list " + listName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to perform list operation: " + e.getMessage());
        }
    }
}