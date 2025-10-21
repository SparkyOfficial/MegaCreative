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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;

/**
 * Action for creating a scoreboard.
 * This action retrieves parameters from the new parameter system.
 */
@BlockMeta(id = "createScoreboard", displayName = "§aCreate Scoreboard", type = BlockType.ACTION)
public class CreateScoreboardAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue titleValue = block.getParameter("title");
            if (titleValue == null || titleValue.isEmpty()) {
                return ExecutionResult.error("No scoreboard title provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            String scoreboardTitle = resolvedTitle.asString();
            
            // Removed redundant null check - static analysis flagged it as always non-null when this method is called
            if (scoreboardTitle.isEmpty()) {
                return ExecutionResult.error("Invalid scoreboard title");
            }

            
            Scoreboard scoreboard = player.getScoreboard();
            
            Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                objective = scoreboard.registerNewObjective("main", "dummy", scoreboardTitle);
            } else {
                objective.setDisplayName(scoreboardTitle);
            }
            
            
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            
            
            player.setScoreboard(scoreboard);

            return ExecutionResult.success("Scoreboard created successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create scoreboard: " + e.getMessage());
        }
    }
}