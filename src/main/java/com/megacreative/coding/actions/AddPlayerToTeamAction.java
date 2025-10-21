package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.ParameterUtils;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Action for adding a player to a team.
 * This action retrieves parameters from the new parameter system with fallback to container configuration.
 * 
 * Действие для добавления игрока в команду.
 * Это действие получает параметры из новой системы параметров с резервным вариантом на конфигурацию контейнера.
 * 
 * @author Андрій Budильников
 */
@BlockMeta(id = "addPlayerToTeam", displayName = "§aAdd Player To Team", type = BlockType.ACTION)
public class AddPlayerToTeamAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            String teamName = ParameterUtils.getStringParameter(block, context, "teamName", "teamName", "");
            String targetPlayer = ParameterUtils.getStringParameter(block, context, "targetPlayer", "targetPlayer", "");
            
            
            if (targetPlayer == null || targetPlayer.isEmpty()) {
                targetPlayer = player.getName();
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            teamName = resolver.resolveString(context, teamName);
            targetPlayer = resolver.resolveString(context, targetPlayer);
            
            if (teamName == null || teamName.isEmpty()) {
                return ExecutionResult.error("Invalid team name");
            }

            
            Scoreboard scoreboard = player.getScoreboard();
            Team team = scoreboard.getTeam(teamName);
            
            if (team == null) {
                return ExecutionResult.error("Team not found: " + teamName);
            }
            
            
            team.addEntry(targetPlayer);

            return ExecutionResult.success("Player added to team successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to add player to team: " + e.getMessage());
        }
    }
}
