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
 * Action for incrementing a score on a scoreboard.
 * This action increments a score for a specific key on the player's scoreboard.
 */
public class IncrementScoreAction implements BlockAction {

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

            // Get the increment parameter from the block (default to 1)
            int increment = 1;
            DataValue incrementValue = block.getParameter("increment");
            if (incrementValue != null) {
                try {
                    increment = incrementValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default increment if parsing fails
                }
            }

            // Resolve any placeholders in the key
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedKey = resolver.resolve(context, keyValue);
            
            // Parse key parameter
            String key = resolvedKey.asString();
            if (key == null || key.isEmpty()) {
                return ExecutionResult.error("Key is empty or null");
            }

            // Increment the score
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.incrementPlayerScore(player, key, increment);
                return ExecutionResult.success("Incremented score for '" + key + "' by " + increment);
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to increment score: " + e.getMessage());
        }
    }
}