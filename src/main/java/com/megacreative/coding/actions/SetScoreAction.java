package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.managers.GameScoreboardManager;
import org.bukkit.entity.Player;

/**
 * Action for setting a score on a scoreboard.
 * This action sets a score for a specific key on the player's scoreboard.
 */
public class SetScoreAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the key parameter from the block
            DataValue keyValue = block.getParameter("key");
            if (keyValue == null) {
                return ExecutionResult.error("Key parameter is missing");
            }

            // Get the value parameter from the block
            DataValue valueValue = block.getParameter("value");
            if (valueValue == null) {
                return ExecutionResult.error("Value parameter is missing");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedKey = resolver.resolve(context, keyValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String key = resolvedKey.asString();
            if (key == null || key.isEmpty()) {
                return ExecutionResult.error("Key is empty or null");
            }

            int value;
            try {
                value = resolvedValue.asNumber().intValue();
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid value: " + resolvedValue.asString());
            }

            // Set the score
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.setPlayerScore(player, key, value);
                return ExecutionResult.success("Set score for '" + key + "' to " + value);
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set score: " + e.getMessage());
        }
    }
}