package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class AddPlayerToTeamAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawTeamName = block.getParameter("teamName");
        DataValue rawTargetPlayer = block.getParameter("targetPlayer");
        
        if (rawTeamName == null) {
            return ExecutionResult.error("Parameter 'teamName' is missing");
        }
        
        DataValue teamNameValue = resolver.resolve(context, rawTeamName);
        String teamName = teamNameValue.asString();

        if (teamName == null) {
            return ExecutionResult.error("Team name parameter is null");
        }

        try {
            // Получаем главный скорборд
            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            
            // Ищем команду
            Team team = scoreboard.getTeam(teamName);
            if (team == null) {
                return ExecutionResult.error("Team '" + teamName + "' not found");
            }
            
            // Определяем, какого игрока добавлять в команду
            Player targetPlayer = player; // По умолчанию добавляем текущего игрока
            
            if (rawTargetPlayer != null) {
                DataValue targetPlayerValue = resolver.resolve(context, rawTargetPlayer);
                String targetPlayerName = targetPlayerValue.asString();
                if (targetPlayerName != null && !targetPlayerName.isEmpty()) {
                    targetPlayer = Bukkit.getPlayer(targetPlayerName);
                    if (targetPlayer == null) {
                        return ExecutionResult.error("Player '" + targetPlayerName + "' not found");
                    }
                }
            }
            
            // Добавляем игрока в команду
            team.addEntry(targetPlayer.getName());
            
            player.sendMessage("§a✅ Игрок '" + targetPlayer.getName() + "' добавлен в команду '" + teamName + "'");
            return ExecutionResult.success("Player '" + targetPlayer.getName() + "' added to team '" + teamName + "'");
        } catch (Exception e) {
            return ExecutionResult.error("Error adding player to team: " + e.getMessage());
        }
    }
}