package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.actions.RepeatTriggerAction;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StopRepeatCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public StopRepeatCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            // Останавливаем задачи для текущего игрока
            if (RepeatTriggerAction.hasActiveTask(player.getUniqueId())) {
                RepeatTriggerAction.stopRepeatingTask(player.getUniqueId());
                player.sendMessage("§a✅ Ваши повторяющиеся задачи остановлены!");
            } else {
                player.sendMessage("§eℹ У вас нет активных повторяющихся задач.");
            }
        } else if (args.length == 1 && args[0].equalsIgnoreCase("all")) {
            // Останавливаем все задачи (только для операторов)
            if (player.isOp()) {
                int stoppedCount = RepeatTriggerAction.stopAllRepeatingTasks();
                player.sendMessage("§a✅ Остановлено " + stoppedCount + " повторяющихся задач!");
            } else {
                player.sendMessage("§c❌ У вас нет прав для остановки всех задач!");
            }
        } else {
            player.sendMessage("§eИспользование:");
            player.sendMessage("§7/stoprepeat §8- остановить ваши повторяющиеся задачи");
            player.sendMessage("§7/stoprepeat all §8- остановить все задачи (только для операторов)");
        }
        
        return true;
    }
} 