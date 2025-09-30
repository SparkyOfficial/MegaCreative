package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

/**
 * Action for getting a global variable.
 * This action retrieves variable parameters from the new parameter system and gets the global variable.
 */
public class GetGlobalVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get variable parameters from the new parameter system
            DataValue nameValue = block.getParameter("name");
            DataValue targetValue = block.getParameter("target");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (targetValue == null || targetValue.isEmpty()) {
                return ExecutionResult.error("No target variable provided");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String targetVar = resolvedTarget.asString();
            
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            if (targetVar == null || targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            DataValue globalVar = variableManager.getGlobalVariable(varName);
            Object varValue = globalVar != null ? globalVar.getValue() : "";
            
            // Store the value in the target variable (using local scope)
            variableManager.setLocalVariable(context.getScriptId(), targetVar, DataValue.of(varValue));
            
            context.getPlugin().getLogger().info("Getting global variable " + varName + " into " + targetVar);
            
            return ExecutionResult.success("Global variable retrieved successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get global variable: " + e.getMessage());
        }
    }
}