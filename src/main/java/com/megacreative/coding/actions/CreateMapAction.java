package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

import java.util.HashMap;
import java.util.Map;

/**
 * Action to create a map variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "createMap", displayName = "§bCreate Map", type = BlockType.ACTION)
public class CreateMapAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameter
            DataValue mapNameValue = block.getParameter("mapName");
            
            if (mapNameValue == null) {
                return ExecutionResult.error("Missing required parameter: mapName");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMapName = resolver.resolve(context, mapNameValue);
            
            String mapName = resolvedMapName.asString();
            
            // Create empty map
            Map<Object, Object> map = new HashMap<>();
            
            // Set map variable
            context.setVariable(mapName, map);
            
            return ExecutionResult.success("Created map " + mapName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create map: " + e.getMessage());
        }
    }
}