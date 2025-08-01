package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CommandAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        if (block == null) return;

        String command = (String) block.getParameter("command");
        if (command != null) {
            // Заменяем %player% на имя игрока
            if (player != null) {
                command = command.replace("%player%", player.getName());
            }
            
            // Выполняем команду от имени игрока или консоли
            if (player != null) {
                Bukkit.dispatchCommand(player, command);
            } else {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
        }
    }
} 