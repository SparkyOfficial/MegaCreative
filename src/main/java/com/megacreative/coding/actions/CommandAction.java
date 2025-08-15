package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Bukkit;
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
            
            // Выполняем команду от имени консоли
            boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
            
            if (success) {
                player.sendMessage("§a✓ Команда выполнена: " + finalCommand);
            } else {
                player.sendMessage("§c✗ Ошибка выполнения команды: " + finalCommand);
            }
            
        } catch (Exception e) {
            player.sendMessage("§cОшибка выполнения команды: " + e.getMessage());
        }
    }
} 