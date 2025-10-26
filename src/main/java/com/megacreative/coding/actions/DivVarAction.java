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
 * Action to divide a variable by a value
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "divVar", displayName = "§bDivide Variable", type = BlockType.ACTION)
public class DivVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || valueValue == null) {
                return ExecutionResult.error("Missing required parameters: name, value");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            String name = resolvedName.asString();
            double value = resolvedValue.asNumber().doubleValue();
            
            // Check for division by zero
            if (value == 0.0) {
                return ExecutionResult.error("Cannot divide by zero");
            }
            
            // Get current variable value
            DataValue currentValue = context.getVariableAsDataValue(name);
            double current = 0.0;
            if (currentValue != null && !currentValue.isEmpty()) {
                try {
                    current = currentValue.asNumber().doubleValue();
                } catch (NumberFormatException e) {
                    // If current value is not a number, treat it as 0
                }
            }
            
            // Divide values
            double result = current / value;
            
            // Set variable
            context.setVariable(name, String.valueOf(result));
            
            return ExecutionResult.success("Divided variable " + name + " by " + value + ", result: " + result);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to divide variable: " + e.getMessage());
        }
    }
}