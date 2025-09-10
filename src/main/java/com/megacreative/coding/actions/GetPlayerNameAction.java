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

/**
 * Action for getting a player's name.
 * This action retrieves the player's name and stores it in a variable.
 */
public class GetPlayerNameAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the target variable name parameter from the block
            DataValue targetValue = block.getParameter("target");
            if (targetValue == null) {
                return ExecutionResult.error("Target variable name parameter is missing");
            }

            // Resolve any placeholders in the target variable name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse target parameter
            String targetName = resolvedTarget.asString();
            if (targetName == null || targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is empty or null");
            }

            // Get the player's name
            String playerName = player.getName();
            
            // Store the player's name in the target variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                variableManager.setVariable(targetName, DataValue.of(playerName), VariableScope.LOCAL, scriptId);
                return ExecutionResult.success("Player name '" + playerName + "' stored in '" + targetName + "'");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get player name: " + e.getMessage());
        }
    }
}