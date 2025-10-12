package com.megacreative.coding.actions;

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
            
            DataValue scopeValue = block.getParameter("scope", DataValue.of("LOCAL"));
            DataValue filterValue = block.getParameter("filter", DataValue.of(""));
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedScope = resolver.resolve(context, scopeValue);
            DataValue resolvedFilter = resolver.resolve(context, filterValue);
            
            String scope = resolvedScope.asString().toUpperCase();
            String filter = resolvedFilter.asString();
            
            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available.");
            }
            
            
            Map<String, DataValue> variables;
            
            switch (scope) {
                case "PLAYER":
                    variables = variableManager.getPlayerVariables(player.getUniqueId());
                    break;
                case "LOCAL":
                    
                    player.sendMessage("§a=== Variable Inspector (" + scope + ") ===");
                    player.sendMessage("§7Local scope variables are not directly accessible through this inspector.");
                    player.sendMessage("§a================================");
                    return ExecutionResult.success("Variable inspection completed.");
                case "GLOBAL":
                    
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
            
            
            player.sendMessage("§a=== Variable Inspector (" + scope + ") ===");
            
            if (variables == null || variables.isEmpty()) {
                player.sendMessage("§7No variables found in this scope.");
            } else {
                for (Map.Entry<String, DataValue> entry : variables.entrySet()) {
                    String varName = entry.getKey();
                    DataValue varValue = entry.getValue();
                    
                    
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