package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

/**
 * Action for setting a server variable.
 * This action retrieves variable parameters from the new parameter system and sets the server variable.
 */
public class SetServerVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get variable parameters from the new parameter system
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No value provided");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String valueStr = resolvedValue.asString();
            
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            // Get the variable manager to set the actual variable
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            variableManager.setServerVariable(varName, DataValue.of(valueStr));
            
            context.getPlugin().getLogger().info("Setting server variable " + varName + " to " + valueStr);
            
            return ExecutionResult.success("Server variable set successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set server variable: " + e.getMessage());
        }
    }
}