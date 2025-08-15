package com.megacreative.commands;

import com.megacreative.MegaCreative;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public MainCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "help" -> sendHelp(player);
            case "reload" -> {
                if (!player.hasPermission("megacreative.admin")) {
                    player.sendMessage("§cУ вас нет прав на это действие!");
                    return true;
                }
                plugin.getConfigManager().loadConfig();
                player.sendMessage("§aКонфигурация перезагружена!");
            }
            case "info" -> sendInfo(player);
            default -> sendHelp(player);
        }
        
        return true;
    }
    
    private void sendHelp(Player player) {
        player.sendMessage("§8§m                    §r §b§lMegaCreative §8§m                    ");
        player.sendMessage("§7/megacreative help §8- §fПоказать эту справку");
        player.sendMessage("§7/myworlds §8- §fОткрыть меню ваших миров");
        player.sendMessage("§7/worldbrowser §8- §fОткрыть браузер миров");
        player.sendMessage("§7/join <ID> §8- §fПрисоединиться к миру по ID");
        player.sendMessage("§7/play §8- §fПереключиться в режим игры");
        player.sendMessage("§7/build §8- §fПереключиться в режим строительства");
        player.sendMessage("§7/dev §8- §fПереключиться в режим разработки");
        player.sendMessage("§8§m                                                        ");
    }
    
    private void sendInfo(Player player) {
        player.sendMessage("§8§m                    §r §b§lMegaCreative §8§m                    ");
        player.sendMessage("§7Версия: §f1.0.0");
        player.sendMessage("§7Автор: §fMegaCreative Team");
        player.sendMessage("§7Ваших миров: §f" + plugin.getWorldManager().getPlayerWorldCount(player) + "/5");
        player.sendMessage("§8§m                                                        ");
    }
}
