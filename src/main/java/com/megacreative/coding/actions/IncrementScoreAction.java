package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.managers.GameScoreboardManager;
import org.bukkit.entity.Player;

public class IncrementScoreAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawKey = block.getParameter("key");
        DataValue rawIncrement = block.getParameter("increment");
        
        if (rawKey == null) {
            return ExecutionResult.error("Parameter 'key' is missing");
        }
        
        if (rawIncrement == null) {
            return ExecutionResult.error("Parameter 'increment' is missing");
        }
        
        DataValue keyValue = resolver.resolve(context, rawKey);
        DataValue incrementValue = resolver.resolve(context, rawIncrement);
        
        String key = keyValue.asString();
        String incrementStr = incrementValue.asString();

        if (key == null) {
            return ExecutionResult.error("Key parameter is null");
        }
        
        if (incrementStr == null) {
            return ExecutionResult.error("Increment parameter is null");
        }

        try {
            int increment = Integer.parseInt(incrementStr);
            
            // Получаем GameScoreboardManager из ServiceRegistry
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getService(GameScoreboardManager.class);
            if (scoreboardManager == null) {
                return ExecutionResult.error("Failed to get ScoreboardManager");
            }
            
            // Увеличиваем значение в скорборде игрока
            int newValue = scoreboardManager.incrementPlayerScore(player, key, increment);
            
            player.sendMessage("§a✅ Значение '" + key + "' увеличено на " + increment + " (новое значение: " + newValue + ")");
            return ExecutionResult.success("Score '" + key + "' incremented by " + increment);
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid increment parameter: " + incrementStr);
        } catch (Exception e) {
            return ExecutionResult.error("Error incrementing score: " + e.getMessage());
        }
    }
}