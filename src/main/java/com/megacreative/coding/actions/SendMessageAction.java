package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.TextValue;
import org.bukkit.entity.Player;

/**
 * Действие для отправки сообщения игроку.
 * Поддерживает плейсхолдеры в сообщениях и использует типобезопасную систему DataValue.
 * 
 * Примеры использования:
 * - sendMessage("Привет, %player%!") - отправляет "Привет, ИмяИгрока!"
 * - sendMessage("У вас ${money} монет") - отправляет "У вас 100 монет"
 * - sendMessage("Координаты: %x%, %y%, %z%") - отправляет координаты игрока
 */
public class SendMessageAction implements BlockAction {
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        // Create ParameterResolver with ExecutionContext
        ParameterResolver resolver = new ParameterResolver(context);
        
        String message = null;
        
        // Получаем сообщение из параметра (приоритет 1)
        DataValue messageValue = block.getParameter("message");
        if (messageValue != null && !messageValue.isEmpty()) {
            DataValue resolved = resolver.resolve(context, messageValue);
            message = resolved.asString();
        }
        
        // Если параметра нет, получаем из виртуального инвентаря (приоритет 2)
        if ((message == null || message.isEmpty())) {
            var messageItem = block.getItemFromSlot("message_slot");
            if (messageItem != null && messageItem.hasItemMeta() && messageItem.getItemMeta().hasDisplayName()) {
                DataValue itemNameValue = new TextValue(messageItem.getItemMeta().getDisplayName());
                DataValue resolved = resolver.resolve(context, itemNameValue);
                message = resolved.asString();
            } else {
                // Fallback на старый способ (слот 0)
                messageItem = block.getConfigItem(0);
                if (messageItem != null && messageItem.hasItemMeta() && messageItem.getItemMeta().hasDisplayName()) {
                    DataValue itemNameValue = new TextValue(messageItem.getItemMeta().getDisplayName());
                    DataValue resolved = resolver.resolve(context, itemNameValue);
                    message = resolved.asString();
                }
            }
        }
        
        // Если сообщение все еще не найдено, показываем ошибку
        if (message == null || message.isEmpty()) {
            player.sendMessage("§cОшибка: не указано сообщение для отправки!");
            return;
        }
        
        // Разрешаем плейсхолдеры в сообщении (для совместимости)
        String resolvedMessage = PlaceholderResolver.resolve(message, context);
        
        // Отправляем сообщение игроку
        player.sendMessage(resolvedMessage);
    }
} 