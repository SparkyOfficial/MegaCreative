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
            // Get parameters from the new parameter system
            DataValue targetValue = block.getParameter("target");
            
            if (targetValue == null || targetValue.isEmpty()) {
                return ExecutionResult.error("No target variable provided");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTarget = resolver.resolve(context, targetValue);
            
            // Parse parameters
            String targetVar = resolvedTarget.asString();
            
            if (targetVar == null || targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            // Get the player's name
            String playerName = player.getName();

            // 🎆 ENHANCED: Actually set the variable using VariableManager
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
            if (variableManager == null) {
                return ExecutionResult.error("Variable manager not available");
            }
            
            // Set the variable for the player
            DataValue dataValue = DataValue.of(playerName);
            variableManager.setPlayerVariable(player.getUniqueId(), targetVar, dataValue);
            
            context.getPlugin().getLogger().info("💾 Player name stored: " + playerName + " -> " + targetVar + " for player " + player.getName());
            
            return ExecutionResult.success("Player name '" + playerName + "' stored in variable '" + targetVar + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get player name: " + e.getMessage());
        }
    }
}
