package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;

/**
 * Action for getting a server variable.
 * This action retrieves variable parameters from the new parameter system and gets the server variable.
 */
public class GetServerVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            
            DataValue nameValue = block.getParameter("name");
            DataValue targetValue = block.getParameter("target");
            
            if (nameValue == null || nameValue.isEmpty()) {
                return ExecutionResult.error("No variable name provided");
            }
            
            if (targetValue == null || targetValue.isEmpty()) {
                return ExecutionResult.error("No target variable provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            
            String varName = resolvedName.asString();
            String targetVar = resolvedTarget.asString();
            
            if (varName == null || varName.isEmpty()) {
                return ExecutionResult.error("Invalid variable name");
            }

            if (targetVar == null || targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            DataValue serverVar = variableManager.getServerVariable(varName);
            Object varValue = serverVar != null ? serverVar.getValue() : "";
            
            
            variableManager.setLocalVariable(context.getScriptId(), targetVar, DataValue.of(varValue));
            
            context.getPlugin().getLogger().info("Getting server variable " + varName + " into " + targetVar);
            
            return ExecutionResult.success("Server variable retrieved successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get server variable: " + e.getMessage());
        }
    }
}