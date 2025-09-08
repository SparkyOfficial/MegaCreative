package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExecuteAsyncCommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        // Получаем параметры из блока
        String command = block.getParameter("command", DataValue.of("")).asString();
        
        if (command == null || command.trim().isEmpty()) {
            return ExecutionResult.error("Command cannot be empty");
        }
        
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player is not available");
        }
        
        // Создаем final копии для использования в лямбде
        final String finalCommand = command.replace("%player%", player.getName())
                                          .replace("%world%", player.getWorld().getName())
                                          .replace("%x%", String.valueOf(player.getLocation().getBlockX()))
                                          .replace("%y%", String.valueOf(player.getLocation().getBlockY()))
                                          .replace("%z%", String.valueOf(player.getLocation().getBlockZ()));
        final String playerName = player.getName();
        
        // Выполняем команду асинхронно
        Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
            try {
                boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
                if (success) {
                    context.getPlugin().getLogger().info("Async command executed successfully: " + finalCommand);
                } else {
                    context.getPlugin().getLogger().warning("Failed to execute async command: " + finalCommand);
                }
            } catch (Exception e) {
                context.getPlugin().getLogger().severe("Error executing async command '" + finalCommand + "' for player " + playerName + ": " + e.getMessage());
            }
        });
        
        return ExecutionResult.success("Async command scheduled for execution");
    }
}