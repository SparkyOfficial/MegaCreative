package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to create and execute a custom function
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "customFunction", displayName = "§bCustom Function", type = BlockType.ACTION)
public class CustomFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue functionNameValue = block.getParameter("functionName");
            DataValue paramCountValue = block.getParameter("paramCount");
            
            if (functionNameValue == null) {
                return ExecutionResult.error("Missing required parameter: functionName");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedFunctionName = resolver.resolve(context, functionNameValue);
            
            String functionName = resolvedFunctionName.asString();
            
            // Get parameter count (default to 0)
            int paramCount = 0;
            if (paramCountValue != null) {
                DataValue resolvedParamCount = resolver.resolve(context, paramCountValue);
                paramCount = Math.max(0, resolvedParamCount.asNumber().intValue());
            }
            
            // Create custom function (placeholder implementation)
            // In a real implementation, this would create a new function with the specified parameters
            context.getPlugin().getLogger().info("Creating custom function: " + functionName + " with " + paramCount + " parameters");
            
            return ExecutionResult.success("Created custom function " + functionName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create custom function: " + e.getMessage());
        }
    }
}