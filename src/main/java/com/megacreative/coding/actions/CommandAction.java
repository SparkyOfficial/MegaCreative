package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.utils.SafeCommandExecutor;
import org.bukkit.entity.Player;

public class CommandAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawCommand = block.getParameter("command");

        String commandStr = ParameterResolver.resolve(context, rawCommand);

        if (commandStr == null || commandStr.isEmpty()) return;

        try {
            // Заменяем плейсхолдеры
            String finalCommand = commandStr
                .replace("%player%", player.getName())
                .replace("%world%", player.getWorld().getName());
            
            // Безопасное выполнение команды от имени игрока
            boolean success = SafeCommandExecutor.executeCommand(player, finalCommand);
            
            if (success) {
                player.sendMessage("§a✓ Команда выполнена: " + finalCommand);
            }
            // Сообщения об ошибках уже отправляются в SafeCommandExecutor
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка выполнения команды: " + e.getMessage());
        }
    }
} 