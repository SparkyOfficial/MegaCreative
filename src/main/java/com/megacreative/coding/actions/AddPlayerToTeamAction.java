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
import org.bukkit.scoreboard.Team;

/**
 * Action to add a player to a team
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "addPlayerToTeam", displayName = "§bAdd Player to Team", type = BlockType.ACTION)
public class AddPlayerToTeamAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue teamNameValue = block.getParameter("teamName");
            DataValue targetPlayerValue = block.getParameter("targetPlayer");
            
            if (teamNameValue == null) {
                return ExecutionResult.error("Missing required parameter: teamName");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTeamName = resolver.resolve(context, teamNameValue);
            
            String teamName = resolvedTeamName.asString();
            
            // Get target player (default to current player)
            Player targetPlayer = player;
            if (targetPlayerValue != null) {
                DataValue resolvedTargetPlayer = resolver.resolve(context, targetPlayerValue);
                String targetPlayerName = resolvedTargetPlayer.asString();
                targetPlayer = context.getPlugin().getServer().getPlayer(targetPlayerName);
                
                if (targetPlayer == null) {
                    return ExecutionResult.error("Player not found: " + targetPlayerName);
                }
            }
            
            // Get player's scoreboard
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return ExecutionResult.error("Player has no scoreboard");
            }
            
            // Get team
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                return ExecutionResult.error("Team not found: " + teamName);
            }
            
            // Add player to team
            team.addEntry(targetPlayer.getName());
            
            return ExecutionResult.success("Added player " + targetPlayer.getName() + " to team " + teamName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to add player to team: " + e.getMessage());
        }
    }
}