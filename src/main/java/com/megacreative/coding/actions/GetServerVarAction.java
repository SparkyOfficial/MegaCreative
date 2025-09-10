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
 * Action for getting a server variable.
 * This action retrieves a server variable and stores it in another variable or context.
 */
public class GetServerVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the variable name parameter from the block
            DataValue nameValue = block.getParameter("name");
            if (nameValue == null) {
                return ExecutionResult.error("Variable name parameter is missing");
            }

            // Get the target variable name parameter from the block
            DataValue targetValue = block.getParameter("target");
            if (targetValue == null) {
                return ExecutionResult.error("Target variable name parameter is missing");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Variable name is empty or null");
            }
            
            String targetName = resolvedTarget.asString();
            if (targetName == null || targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is empty or null");
            }

            // Get the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Use server scope
                DataValue value = variableManager.getVariable(varName, VariableScope.SERVER, "server");
                if (value != null) {
                    // Store in target variable (using local scope by default)
                    String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                    variableManager.setVariable(targetName, value, VariableScope.LOCAL, scriptId);
                    return ExecutionResult.success("Server variable '" + varName + "' retrieved and stored in '" + targetName + "' successfully");
                } else {
                    return ExecutionResult.error("Server variable '" + varName + "' not found");
                }
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get server variable: " + e.getMessage());
        }
    }
}