package com.megacreative.coding.actions.variable;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import org.bukkit.entity.Player;
import java.util.Map;
import java.util.UUID;

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
            VariableManager variableManager = context.getPlugin().getVariableManager();
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
                    // For local scope, we don't have a direct method, so we'll show a message
                    player.sendMessage("§a=== Variable Inspector (" + scope + ") ===");
                    player.sendMessage("§7Local scope variables are not directly accessible through this inspector.");
                    player.sendMessage("§a================================");
                    return ExecutionResult.success("Variable inspection completed.");
                case "GLOBAL":
                    // For global scope, we don't have a direct method, so we'll show a message
                    player.sendMessage("§a=== Variable Inspector (" + scope + ") ===");
                    player.sendMessage("§7Global scope variables are not directly accessible through this inspector.");
                    player.sendMessage("§a================================");
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
            
            // Send variable information to the player
            player.sendMessage("§a=== Variable Inspector (" + scope + ") ===");
            
            if (variables == null || variables.isEmpty()) {
                player.sendMessage("§7No variables found in this scope.");
            } else {
                for (Map.Entry<String, DataValue> entry : variables.entrySet()) {
                    String varName = entry.getKey();
                    DataValue varValue = entry.getValue();
                    
                    // Apply filter if specified
                    if (filter.isEmpty() || varName.contains(filter)) {
                        player.sendMessage("§e" + varName + "§7: " + varValue.getDescription());
                    }
                }
            }
            
            player.sendMessage("§a================================");
            
            return ExecutionResult.success("Variable inspection completed.");

        } catch (Exception e) {
            return ExecutionResult.error("Error during variable inspection: " + e.getMessage());
        }
    }
}