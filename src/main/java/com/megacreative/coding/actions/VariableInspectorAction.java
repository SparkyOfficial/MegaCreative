package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;

import java.util.Map;

/**
 * Action to inspect variables for debugging
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "variableInspector", displayName = "§bVariable Inspector", type = BlockType.ACTION)
public class VariableInspectorAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters
            DataValue scopeValue = block.getParameter("scope");
            DataValue filterValue = block.getParameter("filter");
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            
            String scope = "local";
            if (scopeValue != null) {
                DataValue resolvedScope = resolver.resolve(context, scopeValue);
                scope = resolvedScope.asString();
            }
            
            String filter = "";
            if (filterValue != null) {
                DataValue resolvedFilter = resolver.resolve(context, filterValue);
                filter = resolvedFilter.asString();
            }
            
            // Get variables based on scope
            Map<String, Object> variables = null;
            switch (scope.toLowerCase()) {
                case "local":
                    // Get local variables
                    variables = context.getVariables();
                    break;
                case "player":
                    // Player variables would be handled differently
                    break;
                case "world":
                case "global":
                    // Global variables would be handled differently
                    break;
                case "server":
                    // Server variables would be handled differently
                    break;
                default:
                    return ExecutionResult.error("Invalid scope: " + scope);
            }
            
            // Log variables for debugging
            if (variables != null) {
                context.getPlugin().getLogger().info("Variable Inspector - Scope: " + scope);
                for (Map.Entry<String, Object> entry : variables.entrySet()) {
                    if (filter.isEmpty() || entry.getKey().contains(filter)) {
                        context.getPlugin().getLogger().info("  " + entry.getKey() + " = " + entry.getValue());
                    }
                }
            }
            
            return ExecutionResult.success("Inspected variables in scope " + scope);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to inspect variables: " + e.getMessage());
        }
    }
}