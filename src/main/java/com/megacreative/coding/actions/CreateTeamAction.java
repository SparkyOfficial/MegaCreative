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
 * Action for creating a team.
 * This action retrieves parameters from the new parameter system.
 */
@BlockMeta(id = "createTeam", displayName = "§aCreate Team", type = BlockType.ACTION)
public class CreateTeamAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue teamNameValue = block.getParameter("teamName");
            DataValue displayNameValue = block.getParameter("displayName");
            DataValue prefixValue = block.getParameter("prefix");
            DataValue suffixValue = block.getParameter("suffix");
            
            if (teamNameValue == null || teamNameValue.isEmpty()) {
                return ExecutionResult.error("No team name provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTeamName = resolver.resolve(context, teamNameValue);
            DataValue resolvedDisplayName = resolver.resolve(context, displayNameValue);
            DataValue resolvedPrefix = resolver.resolve(context, prefixValue);
            DataValue resolvedSuffix = resolver.resolve(context, suffixValue);
            
            
            String teamName = resolvedTeamName.asString();
            String displayName = resolvedDisplayName.asString();
            String prefix = resolvedPrefix.asString();
            String suffix = resolvedSuffix.asString();
            
            if (teamName == null || teamName.isEmpty()) {
                return ExecutionResult.error("Invalid team name");
            }

            
            Scoreboard scoreboard = player.getScoreboard();
            Team team = scoreboard.registerNewTeam(teamName);
            
            
            if (displayName != null && !displayName.isEmpty()) {
                team.setDisplayName(displayName);
            }
            
            if (prefix != null && !prefix.isEmpty()) {
                team.setPrefix(prefix);
            }
            
            if (suffix != null && !suffix.isEmpty()) {
                team.setSuffix(suffix);
            }

            return ExecutionResult.success("Team created successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create team: " + e.getMessage());
        }
    }
}