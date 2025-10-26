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
 * Action to generate a random number and store it in a variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "randomNumber", displayName = "§bRandom Number", type = BlockType.ACTION)
public class RandomNumberAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue minValue = block.getParameter("min");
            DataValue maxValue = block.getParameter("max");
            DataValue variableValue = block.getParameter("variable");
            
            if (variableValue == null) {
                return ExecutionResult.error("Missing required parameter: variable");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedVariable = resolver.resolve(context, variableValue);
            
            String variableName = resolvedVariable.asString();
            
            // Get min and max values (default to 1 and 100)
            int min = 1;
            int max = 100;
            
            if (minValue != null) {
                DataValue resolvedMin = resolver.resolve(context, minValue);
                min = resolvedMin.asNumber().intValue();
            }
            
            if (maxValue != null) {
                DataValue resolvedMax = resolver.resolve(context, maxValue);
                max = resolvedMax.asNumber().intValue();
            }
            
            // Ensure min <= max
            if (min > max) {
                int temp = min;
                min = max;
                max = temp;
            }
            
            // Generate random number
            int randomNumber = min + (int)(Math.random() * (max - min + 1));
            
            // Store in variable
            context.setVariable(variableName, String.valueOf(randomNumber));
            
            return ExecutionResult.success("Generated random number " + randomNumber + " and stored in variable '" + variableName + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to generate random number: " + e.getMessage());
        }
    }
}