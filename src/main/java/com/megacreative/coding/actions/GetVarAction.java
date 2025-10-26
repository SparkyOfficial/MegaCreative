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
 * Action to get a variable and store it in another variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "getVar", displayName = "§bGet Variable", type = BlockType.ACTION)
public class GetVarAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue sourceValue = block.getParameter("source");
            DataValue targetValue = block.getParameter("target");
            
            if (sourceValue == null || targetValue == null) {
                return ExecutionResult.error("Missing required parameters: source, target");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedSource = resolver.resolve(context, sourceValue);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            String source = resolvedSource.asString();
            String target = resolvedTarget.asString();
            
            // Get variable value
            DataValue value = context.getVariableAsDataValue(source);
            String valueStr = value != null ? value.asString() : "";
            
            // Set target variable
            context.setVariable(target, valueStr);
            
            return ExecutionResult.success("Got variable " + source + " and stored in " + target);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get variable: " + e.getMessage());
        }
    }
}