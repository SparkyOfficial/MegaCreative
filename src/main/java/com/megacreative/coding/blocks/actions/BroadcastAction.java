package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Действие для отправки сообщения всем игрокам.
 * Поддерживает получение сообщения из параметра "message".
 */
public class BroadcastAction implements BlockAction {
    
    // Аргумент для получения сообщения
    private final Argument<TextValue> messageArgument = new ParameterArgument("message");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        
        // 1. Получаем сообщение из параметра
        TextValue messageValue = messageArgument.parse(context.getCurrentBlock()).orElse(null);
        if (messageValue == null) {
            if (player != null) {
                player.sendMessage("§cОшибка: не указано сообщение!");
            }
            return;
        }
        
        try {
            // 2. Вычисляем сообщение (обрабатываем плейсхолдеры)
            String message = messageValue.get(context);
            
            // 3. Отправляем сообщение всем игрокам
            Bukkit.broadcastMessage(message);
            
            // 4. Уведомляем игрока об успешной отправке
            if (player != null) {
                player.sendMessage("§a📢 Сообщение отправлено всем игрокам!");
            }
            
        } catch (Exception e) {
            if (player != null) {
                player.sendMessage("§cОшибка в блоке 'Отправить всем': " + e.getMessage());
            }
        }
    }
} 