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
 * Action to get a server variable and store it in a local variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "getServerVar", displayName = "§bGet Server Variable", type = BlockType.ACTION)
public class GetServerVarAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue nameValue = block.getParameter("name");
            DataValue targetValue = block.getParameter("target");
            
            if (nameValue == null || targetValue == null) {
                return ExecutionResult.error("Missing required parameters: name, target");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            String name = resolvedName.asString();
            String target = resolvedTarget.asString();
            
            // Get server variable
            DataValue serverVar = context.getPlugin().getServiceRegistry().getVariableManager().getServerVariable(name);
            String value = serverVar != null ? serverVar.asString() : "";
            
            // Set local variable
            context.setVariable(target, value);
            
            return ExecutionResult.success("Got server variable " + name + " and stored in " + target);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get server variable: " + e.getMessage());
        }
    }
}