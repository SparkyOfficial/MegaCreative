package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.managers.EnemyPlayerRestrictionManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Команда для управления враждебными игроками
 * Позволяет добавлять/удалять игроков из списка враждебных игроков
 */
public class EnemyPlayerCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final EnemyPlayerRestrictionManager enemyPlayerManager;
    
    public EnemyPlayerCommand(MegaCreative plugin) {
        this.plugin = plugin;
        this.enemyPlayerManager = plugin.getServiceRegistry().getEnemyPlayerRestrictionManager();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        // Проверяем права доступа
        if (!player.hasPermission("megacreative.admin")) {
            player.sendMessage("§cУ вас нет прав на использование этой команды!");
            return true;
        }
        
        if (args.length == 0) {
            sendHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "add":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /enemy add <игрок>");
                    return true;
                }
                addEnemyPlayer(player, args[1]);
                break;
                
            case "remove":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /enemy remove <игрок>");
                    return true;
                }
                removeEnemyPlayer(player, args[1]);
                break;
                
            case "list":
                listEnemyPlayers(player);
                break;
                
            case "restricted":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /enemy restricted <add|remove|list> [игрок]");
                    return true;
                }
                handleRestrictedPlayers(player, args);
                break;
                
            default:
                sendHelp(player);
                break;
        }
        
        return true;
    }
    
    private void addEnemyPlayer(Player sender, String playerName) {
        // Добавляем игрока в список враждебных
        enemyPlayerManager.addEnemyPlayer(playerName);
        sender.sendMessage("§aИгрок " + playerName + " добавлен в список враждебных игроков!");
        
        // Если игрок онлайн, кикаем его
        Player target = plugin.getServer().getPlayer(playerName);
        if (target != null && target.isOnline()) {
            target.kickPlayer("§cВы были добавлены в черный список сервера!");
        }
    }
    
    private void removeEnemyPlayer(Player sender, String playerName) {
        // Удаляем игрока из списка враждебных
        enemyPlayerManager.removeEnemyPlayer(playerName);
        sender.sendMessage("§aИгрок " + playerName + " удален из списка враждебных игроков!");
    }
    
    private void listEnemyPlayers(Player sender) {
        Set<String> enemyPlayers = enemyPlayerManager.getEnemyPlayers();
        if (enemyPlayers.isEmpty()) {
            sender.sendMessage("§7Список враждебных игроков пуст.");
            return;
        }
        
        sender.sendMessage("§eВраждебные игроки (" + enemyPlayers.size() + "):");
        for (String playerName : enemyPlayers) {
            sender.sendMessage("§7- §c" + playerName);
        }
    }
    
    private void handleRestrictedPlayers(Player sender, String[] args) {
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "add":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /enemy restricted add <игрок>");
                    return;
                }
                // Note: This would require modifying the config file to persist the change
                sender.sendMessage("§eДобавление игроков с ограниченным доступом требует ручного изменения конфигурации.");
                break;
                
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /enemy restricted remove <игрок>");
                    return;
                }
                // Note: This would require modifying the config file to persist the change
                sender.sendMessage("§eУдаление игроков с ограниченным доступом требует ручного изменения конфигурации.");
                break;
                
            case "list":
                Set<String> restrictedPlayers = enemyPlayerManager.getRestrictedPlayers();
                if (restrictedPlayers.isEmpty()) {
                    sender.sendMessage("§7Список игроков с ограниченным доступом пуст.");
                    return;
                }
                
                sender.sendMessage("§eИгроки с ограниченным доступом (" + restrictedPlayers.size() + "):");
                for (String playerName : restrictedPlayers) {
                    sender.sendMessage("§7- §6" + playerName);
                }
                break;
                
            default:
                sender.sendMessage("§cНеверная подкоманда. Используйте: add, remove, list");
                break;
        }
    }
    
    private void sendHelp(Player sender) {
        sender.sendMessage("§e=== Команды управления враждебными игроками ===");
        sender.sendMessage("§7/enemy add <игрок> §f- Добавить враждебного игрока");
        sender.sendMessage("§7/enemy remove <игрок> §f- Удалить враждебного игрока");
        sender.sendMessage("§7/enemy list §f- Показать список враждебных игроков");
        sender.sendMessage("§7/enemy restricted list §f- Показать список игроков с ограниченным доступом");
        sender.sendMessage("§7Примечание: §fИзменение списка игроков с ограниченным доступом требует ручного изменения config.yml");
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        Player player = (Player) sender;
        
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();
            if (player.hasPermission("megacreative.admin")) {
                completions.add("add");
                completions.add("remove");
                completions.add("list");
                completions.add("restricted");
            }
            return completions;
        }
        
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {
                // Return list of online players
                List<String> playerNames = new ArrayList<>();
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    playerNames.add(onlinePlayer.getName());
                }
                return playerNames;
            }
            
            if (args[0].equalsIgnoreCase("restricted")) {
                List<String> completions = new ArrayList<>();
                completions.add("add");
                completions.add("remove");
                completions.add("list");
                return completions;
            }
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("restricted")) {
            if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
                // Return list of online players
                List<String> playerNames = new ArrayList<>();
                for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                    playerNames.add(onlinePlayer.getName());
                }
                return playerNames;
            }
        }
        
        return new ArrayList<>();
    }
}