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
import org.bukkit.scoreboard.ScoreboardManager;

/**
 * Action to create a scoreboard for a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "createScoreboard", displayName = "§bCreate Scoreboard", type = BlockType.ACTION)
public class CreateScoreboardAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameter
            DataValue titleValue = block.getParameter("title");
            
            if (titleValue == null) {
                return ExecutionResult.error("Missing required parameter: title");
            }
            
            // Resolve parameter
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            String title = resolvedTitle.asString();
            
            // Create scoreboard
            ScoreboardManager manager = context.getPlugin().getServer().getScoreboardManager();
            Scoreboard scoreboard = manager.getNewScoreboard();
            
            // Set scoreboard for player
            player.setScoreboard(scoreboard);
            
            return ExecutionResult.success("Created scoreboard with title: " + title);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create scoreboard: " + e.getMessage());
        }
    }
}