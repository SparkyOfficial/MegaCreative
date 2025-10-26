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
 * Action to call a saved function
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "callFunction", displayName = "§bCall Function", type = BlockType.ACTION)
public class CallFunctionAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue functionValue = block.getParameter("function");
            
            if (functionValue == null) {
                return ExecutionResult.error("Missing required parameter: function");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedFunction = resolver.resolve(context, functionValue);
            
            String functionName = resolvedFunction.asString();
            
            // Call function (placeholder implementation)
            // In a real implementation, this would retrieve and execute the saved function
            context.getPlugin().getLogger().info("Calling function: " + functionName);
            
            return ExecutionResult.success("Called function " + functionName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to call function: " + e.getMessage());
        }
    }
}