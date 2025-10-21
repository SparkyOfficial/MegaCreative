package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

/**
 * Action for incrementing a score on a scoreboard.
 * This action retrieves parameters from the new parameter system and increments a score.
 */
public class IncrementScoreAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue keyValue = block.getParameter("key");
            DataValue incrementValue = block.getParameter("increment");
            
            if (keyValue == null || keyValue.isEmpty()) {
                return ExecutionResult.error("No score key provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedKey = resolver.resolve(context, keyValue);
            DataValue resolvedIncrement = resolver.resolve(context, incrementValue);
            
            
            String key = resolvedKey.asString();
            String incrementStr = resolvedIncrement.asString();
            
            // Removed redundant null checks - static analysis flagged them as always non-null when this method is called
            if (key.isEmpty()) {
                return ExecutionResult.error("Invalid score key");
            }

            
            int increment = 1; 
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            if (!incrementStr.isEmpty()) {
                try {
                    increment = Integer.parseInt(incrementStr);
                } catch (NumberFormatException e) {
                    return ExecutionResult.error("Invalid increment value: " + incrementStr);
                }
            }

            
            Scoreboard scoreboard = player.getScoreboard();
            
            Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                return ExecutionResult.error("No main objective found on scoreboard");
            }
            
            Score score = objective.getScore(key);
            score.setScore(score.getScore() + increment);

            return ExecutionResult.success("Score incremented successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to increment score: " + e.getMessage());
        }
    }
}