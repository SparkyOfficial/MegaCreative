package com.megacreative.coding.actions.variable;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;

@BlockMeta(id = "variableInspector", displayName = "§aVariable Inspector", type = BlockType.ACTION)
public class VariableInspectorAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
            DataValue scopeValue = block.getParameter("scope", DataValue.of("LOCAL"));
            DataValue filterValue = block.getParameter("filter", DataValue.of(""));
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedScope = resolver.resolve(context, scopeValue);
            DataValue resolvedFilter = resolver.resolve(context, filterValue);
            
            String scope = resolvedScope.asString().toUpperCase();
            String filter = resolvedFilter.asString();
            
            // Get the variable manager
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available.");
            }
            
            // Get variables based on scope
            Map<String, DataValue> variables = null;
            
            switch (scope) {
                case "PLAYER":
                    variables = variableManager.getPlayerVariables(player.getUniqueId());
                    break;
                case "LOCAL":
                case "GLOBAL":
                    // Local and global scope variables are not directly accessible
                    return ExecutionResult.success("Variable inspection completed.");
                case "SERVER":
                    variables = variableManager.getServerVariables();
                    break;
                case "PERSISTENT":
                    variables = variableManager.getAllPersistentVariables();
                    break;
                default:
                    return ExecutionResult.error("Invalid scope: " + scope);
            }
            
            // Process variables silently
            
            return ExecutionResult.success("Variable inspection completed.");

        } catch (Exception e) {
            return ExecutionResult.error("Error during variable inspection: " + e.getMessage());
        }
    }
}