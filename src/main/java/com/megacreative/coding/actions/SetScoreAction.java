package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.managers.GameScoreboardManager;
import org.bukkit.entity.Player;

public class SetScoreAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null || block == null) {
            return ExecutionResult.error("Player or block is null");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawKey = block.getParameter("key");
        DataValue rawValue = block.getParameter("value");
        
        if (rawKey == null) {
            return ExecutionResult.error("Parameter 'key' is missing");
        }
        
        if (rawValue == null) {
            return ExecutionResult.error("Parameter 'value' is missing");
        }
        
        DataValue keyValue = resolver.resolve(context, rawKey);
        DataValue valueValue = resolver.resolve(context, rawValue);
        
        String key = keyValue.asString();
        String valueStr = valueValue.asString();

        if (key == null) {
            return ExecutionResult.error("Key parameter is null");
        }
        
        if (valueStr == null) {
            return ExecutionResult.error("Value parameter is null");
        }

        try {
            int value = Integer.parseInt(valueStr);
            
            // Получаем GameScoreboardManager из ServiceRegistry
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getService(GameScoreboardManager.class);
            if (scoreboardManager == null) {
                return ExecutionResult.error("Failed to get ScoreboardManager");
            }
            
            // Устанавливаем значение в скорборде игрока
            boolean success = scoreboardManager.setPlayerScore(player, key, value);
            
            if (success) {
                player.sendMessage("§a✅ Значение '" + key + "' установлено на " + value);
                return ExecutionResult.success("Score '" + key + "' set to " + value);
            } else {
                return ExecutionResult.error("Failed to set score");
            }
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid value parameter: " + valueStr);
        } catch (Exception e) {
            return ExecutionResult.error("Error setting score: " + e.getMessage());
        }
    }
}