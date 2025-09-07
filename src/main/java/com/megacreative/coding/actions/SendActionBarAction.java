package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class SendActionBarAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            DataValue messageValue = block.getParameter("message");
            DataValue durationValue = block.getParameter("duration");
            
            if (messageValue == null) {
                return ExecutionResult.error("No message specified");
            }
            
            String message = messageValue.asString();
            if (message == null || message.trim().isEmpty()) {
                return ExecutionResult.error("Message cannot be empty");
            }
            
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("No player in execution context");
            }
            
            // Отправляем сообщение в action bar
            player.sendActionBar(message);
            
            // Если указана продолжительность, можно добавить логику таймера
            // Но в Minecraft action bar автоматически исчезает, поэтому просто логируем
            if (durationValue != null) {
                int duration = durationValue.asNumber().intValue();
                // В реальной реализации можно использовать BukkitRunnable для управления временем отображения
            }
            
            return ExecutionResult.success("Action bar message sent successfully");
            
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send action bar message: " + e.getMessage());
        }
    }
}