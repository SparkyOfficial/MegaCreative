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
 * Action for setting a score on a scoreboard.
 * This action retrieves parameters from the new parameter system.
 */
public class SetScoreAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue keyValue = block.getParameter("key");
            DataValue valueValue = block.getParameter("value");
            
            if (keyValue == null || keyValue.isEmpty()) {
                return ExecutionResult.error("No score key provided");
            }
            
            if (valueValue == null || valueValue.isEmpty()) {
                return ExecutionResult.error("No score value provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedKey = resolver.resolve(context, keyValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            
            String key = resolvedKey.asString();
            String valueStr = resolvedValue.asString();
            
            if (key == null || key.isEmpty()) {
                return ExecutionResult.error("Invalid score key");
            }

            
            int value;
            try {
                value = Integer.parseInt(valueStr);
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid score value: " + valueStr);
            }

            
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return ExecutionResult.error("No scoreboard found for player");
            }
            
            Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                return ExecutionResult.error("No main objective found on scoreboard");
            }
            
            Score score = objective.getScore(key);
            score.setScore(value);

            return ExecutionResult.success("Score set successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set score: " + e.getMessage());
        }
    }
}
