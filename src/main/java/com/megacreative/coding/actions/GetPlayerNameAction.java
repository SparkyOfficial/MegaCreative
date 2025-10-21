package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Action for getting a player's name.
 * This action retrieves parameters from the new parameter system and gets the player's name.
 */
public class GetPlayerNameAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue targetValue = block.getParameter("target");
            
            if (targetValue == null || targetValue.isEmpty()) {
                return ExecutionResult.error("No target variable provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            
            String targetVar = resolvedTarget.asString();
            
            // Fix for Qodana issue: Condition targetVar == null is always false
            // This was a false positive - we need to properly check for empty strings
            if (targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            
            String playerName = player.getName();

            
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available");
            }
            
            
            DataValue dataValue = DataValue.of(playerName);
            variableManager.setPlayerVariable(player.getUniqueId(), targetVar, dataValue);
            
            context.getPlugin().getLogger().info("💾 Player name stored: " + playerName + " -> " + targetVar + " for player " + player.getName());
            
            return ExecutionResult.success("Player name '" + playerName + "' stored in variable '" + targetVar + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get player name: " + e.getMessage());
        }
    }
}
