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
 * Action for creating a scoreboard.
 * This action creates a scoreboard with a specified title.
 */
public class CreateScoreboardAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the title parameter from the block
            DataValue titleValue = block.getParameter("title");
            if (titleValue == null) {
                return ExecutionResult.error("Title parameter is missing");
            }

            // Resolve any placeholders in the title
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            // Parse title parameter
            String title = resolvedTitle.asString();
            if (title == null || title.isEmpty()) {
                return ExecutionResult.error("Title is empty or null");
            }

            // Create the scoreboard
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.createPlayerScoreboard(player, title);
                return ExecutionResult.success("Created scoreboard with title: " + title);
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create scoreboard: " + e.getMessage());
        }
    }
}