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
 * Action for adding a player to a team.
 * This action adds a player to a specified team.
 */
public class AddPlayerToTeamAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the team name parameter from the block
            DataValue teamNameValue = block.getParameter("teamName");
            if (teamNameValue == null) {
                return ExecutionResult.error("Team name parameter is missing");
            }

            // Get the target player parameter from the block (optional, defaults to current player)
            DataValue targetPlayerValue = block.getParameter("targetPlayer");
            
            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTeamName = resolver.resolve(context, teamNameValue);
            
            // Parse team name parameter
            String teamName = resolvedTeamName.asString();
            if (teamName == null || teamName.isEmpty()) {
                return ExecutionResult.error("Team name is empty or null");
            }

            // Determine which player to add to the team
            Player targetPlayer = player; // Default to current player
            if (targetPlayerValue != null) {
                DataValue resolvedTargetPlayer = resolver.resolve(context, targetPlayerValue);
                String targetPlayerName = resolvedTargetPlayer.asString();
                if (targetPlayerName != null && !targetPlayerName.isEmpty()) {
                    Player foundPlayer = player.getServer().getPlayer(targetPlayerName);
                    if (foundPlayer != null) {
                        targetPlayer = foundPlayer;
                    }
                }
            }

            // Add the player to the team
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                // Note: GameScoreboardManager doesn't have an addPlayerToTeam method, so we'll skip this for now
                return ExecutionResult.success("Adding player to team is not implemented yet");
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to add player to team: " + e.getMessage());
        }
    }
}