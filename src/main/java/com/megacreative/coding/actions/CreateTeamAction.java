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
 * Action to create a team on a scoreboard
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "createTeam", displayName = "§bCreate Team", type = BlockType.ACTION)
public class CreateTeamAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue teamNameValue = block.getParameter("teamName");
            DataValue displayNameValue = block.getParameter("displayName");
            DataValue prefixValue = block.getParameter("prefix");
            DataValue suffixValue = block.getParameter("suffix");
            
            if (teamNameValue == null) {
                return ExecutionResult.error("Missing required parameter: teamName");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTeamName = resolver.resolve(context, teamNameValue);
            
            String teamName = resolvedTeamName.asString();
            
            // Get player's scoreboard
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return ExecutionResult.error("Player has no scoreboard");
            }
            
            // Create or get team
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                team = scoreboard.registerNewTeam(teamName);
            }
            
            // Set team properties
            if (displayNameValue != null) {
                DataValue resolvedDisplayName = resolver.resolve(context, displayNameValue);
                team.setDisplayName(resolvedDisplayName.asString());
            }
            
            if (prefixValue != null) {
                DataValue resolvedPrefix = resolver.resolve(context, prefixValue);
                team.setPrefix(resolvedPrefix.asString());
            }
            
            if (suffixValue != null) {
                DataValue resolvedSuffix = resolver.resolve(context, suffixValue);
                team.setSuffix(resolvedSuffix.asString());
            }
            
            return ExecutionResult.success("Created team " + teamName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create team: " + e.getMessage());
        }
    }
}