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
 * Action to set a score on a scoreboard
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setScore", displayName = "§bSet Score", type = BlockType.ACTION)
public class SetScoreAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue keyValue = block.getParameter("key");
            DataValue valueValue = block.getParameter("value");
            
            if (keyValue == null || valueValue == null) {
                return ExecutionResult.error("Missing required parameters: key, value");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedKey = resolver.resolve(context, keyValue);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            String key = resolvedKey.asString();
            int value = resolvedValue.asNumber().intValue();
            
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
            
            // Set score
            Score score = objective.getScore(key);
            score.setScore(value);
            
            return ExecutionResult.success("Set score " + key + " to " + value);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set score: " + e.getMessage());
        }
    }
}