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
 * Action to set a server variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setServerVar", displayName = "§bSet Server Variable", type = BlockType.ACTION)
public class SetServerVarAction implements BlockAction {
    
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
            String value = resolvedValue.asString();
            
            // Set server variable
            context.getPlugin().getServiceRegistry().getVariableManager().setServerVariable(name, DataValue.of(value));
            
            return ExecutionResult.success("Set server variable " + name + " to " + value);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set server variable: " + e.getMessage());
        }
    }
}