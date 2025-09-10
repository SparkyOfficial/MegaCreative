package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;

/**
 * Action for setting a global variable.
 * This action sets a global variable to a specified value.
 */
public class SetGlobalVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the variable name parameter from the block
            DataValue nameValue = block.getParameter("name");
            if (nameValue == null) {
                return ExecutionResult.error("Variable name parameter is missing");
            }

            // Get the variable value parameter from the block
            DataValue valueValue = block.getParameter("value");
            if (valueValue == null) {
                return ExecutionResult.error("Variable value parameter is missing");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Variable name is empty or null");
            }

            // Set the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Use global scope
                variableManager.setVariable(varName, resolvedValue, VariableScope.GLOBAL, "global");
                return ExecutionResult.success("Global variable '" + varName + "' set successfully");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set global variable: " + e.getMessage());
        }
    }
}