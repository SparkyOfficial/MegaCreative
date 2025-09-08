package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class CreateTeamAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawTeamName = block.getParameter("teamName");
        DataValue rawDisplayName = block.getParameter("displayName");
        DataValue rawPrefix = block.getParameter("prefix");
        DataValue rawSuffix = block.getParameter("suffix");
        
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
            
            // Проверяем, не существует ли уже команда с таким именем
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                return ExecutionResult.error("Team '" + teamName + "' already exists");
            }
            
            // Создаем новую команду
            team = scoreboard.registerNewTeam(teamName);
            
            // Устанавливаем отображаемое имя, префикс и суффикс, если указаны
            if (rawDisplayName != null) {
                DataValue displayNameValue = resolver.resolve(context, rawDisplayName);
                team.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayNameValue.asString()));
            }
            
            if (rawPrefix != null) {
                DataValue prefixValue = resolver.resolve(context, rawPrefix);
                team.setPrefix(ChatColor.translateAlternateColorCodes('&', prefixValue.asString()));
            }
            
            if (rawSuffix != null) {
                DataValue suffixValue = resolver.resolve(context, rawSuffix);
                team.setSuffix(ChatColor.translateAlternateColorCodes('&', suffixValue.asString()));
            }
            
            player.sendMessage("§a✅ Команда '" + teamName + "' создана");
            return ExecutionResult.success("Team '" + teamName + "' created");
        } catch (Exception e) {
            return ExecutionResult.error("Error creating team: " + e.getMessage());
        }
    }
}