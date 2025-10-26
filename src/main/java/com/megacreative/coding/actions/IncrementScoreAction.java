package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;

/**
 * Action to increment a score on a scoreboard
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "incrementScore", displayName = "§bIncrement Score", type = BlockType.ACTION)
public class IncrementScoreAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue keyValue = block.getParameter("key");
            DataValue incrementValue = block.getParameter("increment");
            
            if (keyValue == null) {
                return ExecutionResult.error("Missing required parameter: key");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedKey = resolver.resolve(context, keyValue);
            
            String key = resolvedKey.asString();
            
            // Get increment value (default to 1)
            int increment = 1;
            if (incrementValue != null) {
                DataValue resolvedIncrement = resolver.resolve(context, incrementValue);
                increment = resolvedIncrement.asNumber().intValue();
            }
            
            // Get player's scoreboard
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return ExecutionResult.error("Player has no scoreboard");
            }
            
            // Get or create objective
            Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                objective = scoreboard.registerNewObjective("main", "dummy", "Scoreboard");
                objective.setDisplaySlot(org.bukkit.scoreboard.DisplaySlot.SIDEBAR);
            }
            
            // Get current score and increment it
            Score score = objective.getScore(key);
            int currentValue = score.getScore();
            score.setScore(currentValue + increment);
            
            return ExecutionResult.success("Incremented score " + key + " by " + increment);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to increment score: " + e.getMessage());
        }
    }
}