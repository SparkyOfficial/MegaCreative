package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ItemNameArgument;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.entity.Player;

/**
 * Действие для отправки сообщения игроку.
 * Поддерживает получение сообщения из различных источников:
 * - Из параметра "message"
 * - Из названия предмета в слоте "message_slot"
 * 
 * Примеры использования:
 * - sendMessage("Привет, %player%!") - отправляет "Привет, ИмяИгрока!"
 * - sendMessage("У вас %num:money% монет") - отправляет "У вас 100 монет"
 * - sendMessage("Координаты: %x%, %y%, %z%") - отправляет координаты игрока
 */
public class SendMessageAction implements BlockAction {
    
    // Аргументы для получения сообщения из разных источников
    private final Argument<TextValue> messageFromParameter = new ParameterArgument("message");
    private final Argument<TextValue> messageFromSlot = new ItemNameArgument("message_slot");
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // 1. Пытаемся получить сообщение из параметра
        TextValue messageValue = messageFromParameter.parse(context.getCurrentBlock())
            .orElseGet(() -> 
                // 2. Если не получилось, пытаемся из слота
                messageFromSlot.parse(context.getCurrentBlock()).orElse(null)
            );
        
        if (messageValue == null) {
            player.sendMessage("§cОшибка: не указано сообщение!");
            return;
        }
        
        // 3. Вычисляем значение (TextValue сам обработает плейсхолдеры)
        String message = messageValue.get(context);
        
        // 4. Отправляем сообщение игроку
        player.sendMessage(message);
    }
} 