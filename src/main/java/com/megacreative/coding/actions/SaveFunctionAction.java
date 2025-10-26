package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

/**
 * Action to save a function
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "saveFunction", displayName = "§bSave Function", type = BlockType.ACTION)
public class SaveFunctionAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue functionNameValue = block.getParameter("functionName");
            DataValue descriptionValue = block.getParameter("description");
            
            if (functionNameValue == null) {
                return ExecutionResult.error("Missing required parameter: functionName");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedFunctionName = resolver.resolve(context, functionNameValue);
            
            String functionName = resolvedFunctionName.asString();
            
            // Get description (optional)
            String description = "";
            if (descriptionValue != null) {
                DataValue resolvedDescription = resolver.resolve(context, descriptionValue);
                description = resolvedDescription.asString();
            }
            
            // Save function (placeholder implementation)
            // In a real implementation, this would save the current block chain as a function
            context.getPlugin().getLogger().info("Saving function: " + functionName);
            
            return ExecutionResult.success("Saved function " + functionName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to save function: " + e.getMessage());
        }
    }
}