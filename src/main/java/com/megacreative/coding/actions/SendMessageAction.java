package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.PlaceholderResolver;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.values.types.TextValue;
import org.bukkit.entity.Player;

import javax.inject.Inject;

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
    
    private final ParameterResolver parameterResolver;
    
    @Inject
    public SendMessageAction(ParameterResolver parameterResolver) {
        this.parameterResolver = parameterResolver;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            Player player = context.getPlayer();
            if (player == null) {
                return ExecutionResult.error("No player available to send message to");
            }
            
            String message = getMessage(block, context);
            if (message == null || message.isEmpty()) {
                return ExecutionResult.error("No message specified");
            }
            
            player.sendMessage(message);
            return ExecutionResult.success("Message sent: " + message);
        } catch (Exception e) {
            return ExecutionResult.error("Error sending message: " + e.getMessage());
        }
    }
    
    private String getMessage(CodeBlock block, ExecutionContext context) {
        // Check parameter first (priority 1)
        DataValue messageValue = block.getParameter("message");
        if (messageValue != null && !messageValue.isEmpty()) {
            DataValue resolved = parameterResolver.resolve(context, messageValue);
            return resolved.asString();
        }
        
        // Check virtual inventory (priority 2)
        // Get slot resolver from BlockConfigService
        com.megacreative.services.BlockConfigService configService = 
            context.getPlugin().getServiceRegistry().getBlockConfigService();
        java.util.function.Function<String, Integer> slotResolver = 
            configService != null ? configService.getSlotResolver("sendMessage") : null;
            
        var messageItem = slotResolver != null ? 
            block.getItemFromSlot("message_slot", slotResolver) : null;
        if (messageItem != null && messageItem.hasItemMeta() && messageItem.getItemMeta().hasDisplayName()) {
            DataValue itemNameValue = new TextValue(messageItem.getItemMeta().getDisplayName());
            DataValue resolved = parameterResolver.resolve(context, itemNameValue);
            return resolved.asString();
        }
        
        // Fallback to config item (priority 3)
        messageItem = block.getConfigItem(0);
        if (messageItem != null && messageItem.hasItemMeta() && messageItem.getItemMeta().hasDisplayName()) {
            DataValue itemNameValue = new TextValue(messageItem.getItemMeta().getDisplayName());
            DataValue resolved = parameterResolver.resolve(context, itemNameValue);
            return resolved.asString();
        }
        
        // Message not found
        return null;
    }
} 