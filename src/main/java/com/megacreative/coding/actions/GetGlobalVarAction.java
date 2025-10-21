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
            
            // Removed redundant null checks - static analysis flagged them as always non-null when this method is called

            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            DataValue globalVar = variableManager.getGlobalVariable(varName);
            Object varValue = globalVar != null ? globalVar.getValue() : "";
            
            
            variableManager.setLocalVariable(context.getScriptId(), targetVar, DataValue.of(varValue));
            
            context.getPlugin().getLogger().info("Getting global variable " + varName + " into " + targetVar);
            
            return ExecutionResult.success("Global variable retrieved successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get global variable: " + e.getMessage());
        }
    }
}