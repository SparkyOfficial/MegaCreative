package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

import java.util.Map;

/**
 * Action to perform operations on a map
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "mapOperation", displayName = "§bMap Operation", type = BlockType.ACTION)
public class MapOperationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue mapNameValue = block.getParameter("mapName");
            DataValue operationValue = block.getParameter("operation");
            DataValue keyValue = block.getParameter("key");
            DataValue valueValue = block.getParameter("value");
            
            if (mapNameValue == null || operationValue == null) {
                return ExecutionResult.error("Missing required parameters: mapName, operation");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMapName = resolver.resolve(context, mapNameValue);
            DataValue resolvedOperation = resolver.resolve(context, operationValue);
            
            String mapName = resolvedMapName.asString();
            String operation = resolvedOperation.asString();
            
            // Get map variable
            Object mapObj = context.getVariable(mapName);
            if (!(mapObj instanceof Map)) {
                return ExecutionResult.error("Variable " + mapName + " is not a map");
            }
            
            @SuppressWarnings("unchecked")
            Map<Object, Object> map = (Map<Object, Object>) mapObj;
            
            // Perform operation
            switch (operation.toLowerCase()) {
                case "put":
                    if (keyValue != null && valueValue != null) {
                        DataValue resolvedKey = resolver.resolve(context, keyValue);
                        DataValue resolvedValue = resolver.resolve(context, valueValue);
                        map.put(resolvedKey.asString(), resolvedValue.asString());
                    }
                    break;
                case "get":
                    if (keyValue != null) {
                        DataValue resolvedKey = resolver.resolve(context, keyValue);
                        Object value = map.get(resolvedKey.asString());
                        // Store value in a variable (we'd need a target variable parameter for this)
                    }
                    break;
                case "remove":
                    if (keyValue != null) {
                        DataValue resolvedKey = resolver.resolve(context, keyValue);
                        map.remove(resolvedKey.asString());
                    }
                    break;
                case "keys":
                    // Return keys (we'd need a target variable parameter for this)
                    break;
                case "values":
                    // Return values (we'd need a target variable parameter for this)
                    break;
                default:
                    return ExecutionResult.error("Invalid operation: " + operation);
            }
            
            // Update map variable
            context.setVariable(mapName, map);
            
            return ExecutionResult.success("Performed " + operation + " operation on map " + mapName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to perform map operation: " + e.getMessage());
        }
    }
}