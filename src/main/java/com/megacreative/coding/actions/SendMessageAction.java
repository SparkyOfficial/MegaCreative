package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

/**
 * Действие для отправки сообщения игроку.
 * Поддерживает плейсхолдеры в сообщениях.
 * 
 * Примеры использования:
 * - sendMessage("Привет, %player%!") - отправляет "Привет, ИмяИгрока!"
 * - sendMessage("У вас %num:money% монет") - отправляет "У вас 100 монет"
 * - sendMessage("Координаты: %x%, %y%, %z%") - отправляет координаты игрока
 */
public class SendMessageAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        // Получаем сообщение из параметра
        Object rawMessage = block.getParameter("message");
        String message = ParameterResolver.resolve(context, rawMessage);
        
        if (message == null || message.isEmpty()) {
            // Попробуем получить сообщение из виртуального инвентаря
            var messageItem = block.getItemFromSlot("message_slot");
            if (messageItem != null && messageItem.hasItemMeta() && messageItem.getItemMeta().hasDisplayName()) {
                message = messageItem.getItemMeta().getDisplayName();
            } else {
                // Fallback на старый способ
                messageItem = block.getConfigItem(0);
                if (messageItem != null && messageItem.hasItemMeta() && messageItem.getItemMeta().hasDisplayName()) {
                    message = messageItem.getItemMeta().getDisplayName();
                }
            }
        }
        
        if (message == null || message.isEmpty()) {
            player.sendMessage("§cОшибка: не указано сообщение!");
            return;
        }
        
        // Разрешаем плейсхолдеры в сообщении
        String resolvedMessage = PlaceholderResolver.resolve(message, context);
        
        // Отправляем сообщение игроку
        player.sendMessage(resolvedMessage);
    }
} 