package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

/**
 * Action for setting a global variable.
 * This action retrieves variable parameters from the new parameter system and sets the global variable.
 */
public class SetGlobalVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue nameValue = block.getParameter("name");
            DataValue valueValue = block.getParameter("value");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No value provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            
            String varName = resolvedName.asString();
            // Fix for Qodana issue: Condition varName == null is always false
            // This was a false positive - we need to properly check for empty strings
            if (varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }
            
            // Get the value string for logging and setting the variable
            String valueStr = resolvedValue.asString();

            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            variableManager.setGlobalVariable(varName, DataValue.of(valueStr));
            
            context.getPlugin().getLogger().info("Setting global variable " + varName + " to " + valueStr);
            
            return ExecutionResult.success("Global variable set successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set global variable: " + e.getMessage());
        }
    }
}