package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.managers.GameScoreboardManager;
import org.bukkit.entity.Player;

public class CreateScoreboardAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawTitle = block.getParameter("title");
        
        if (rawTitle == null) {
            return ExecutionResult.error("Parameter 'title' is missing");
        }
        
        DataValue titleValue = resolver.resolve(context, rawTitle);
        String title = titleValue.asString();

        if (title == null) {
            return ExecutionResult.error("Title parameter is null");
        }

        try {
            // Получаем GameScoreboardManager из ServiceRegistry
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getService(GameScoreboardManager.class);
            if (scoreboardManager == null) {
                return ExecutionResult.error("Failed to get GameScoreboardManager");
            }
            
            // Создаем скорборд для игрока
            scoreboardManager.createPlayerScoreboard(player, title);
            
            player.sendMessage("§a✅ Скорборд '" + title + "' создан");
            return ExecutionResult.success("Scoreboard '" + title + "' created");
        } catch (Exception e) {
            return ExecutionResult.error("Error creating scoreboard: " + e.getMessage());
        }
    }
}