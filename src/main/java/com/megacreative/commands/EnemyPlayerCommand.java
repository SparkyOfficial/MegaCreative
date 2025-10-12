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
 * Управление ограничениями доступа для игроков
 *
 * Command for managing enemy players
 * Allows adding/removing players from the enemy player list
 * Access restriction management for players
 *
 * Befehl zur Verwaltung feindlicher Spieler
 * Ermöglicht das Hinzufügen/Entfernen von Spielern zur Liste feindlicher Spieler
 * Zugriffsbeschränkungsverwaltung für Spieler
 */
public class EnemyPlayerCommand implements CommandExecutor, TabCompleter {
    
    private final MegaCreative plugin;
    private final EnemyPlayerRestrictionManager enemyPlayerManager;
    
    /**
     * Инициализирует команду управления враждебными игроками
     * @param plugin основной экземпляр плагина
     *
     * Initializes the enemy player management command
     * @param plugin main plugin instance
     *
     * Initialisiert den Befehl zur Verwaltung feindlicher Spieler
     * @param plugin Haupt-Plugin-Instanz
     */
    public EnemyPlayerCommand(MegaCreative plugin) {
        this.plugin = plugin;
        this.enemyPlayerManager = plugin.getServiceRegistry().getEnemyPlayerRestrictionManager();
    }
    
    /**
     * Обрабатывает выполнение команды управления враждебными игроками
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles enemy player management command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Befehls zur Verwaltung feindlicher Spieler
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        
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
    
    /**
     * Добавляет игрока в список враждебных игроков
     * @param sender игрок, выполняющий команду
     * @param playerName имя игрока для добавления
     *
     * Adds a player to the enemy player list
     * @param sender player executing the command
     * @param playerName name of the player to add
     *
     * Fügt einen Spieler zur Liste feindlicher Spieler hinzu
     * @param sender Spieler, der den Befehl ausführt
     * @param playerName Name des hinzuzufügenden Spielers
     */
    private void addEnemyPlayer(Player sender, String playerName) {
        
        enemyPlayerManager.addEnemyPlayer(playerName);
        sender.sendMessage("§aИгрок " + playerName + " добавлен в список враждебных игроков!");
        
        
        Player target = plugin.getServer().getPlayer(playerName);
        if (target != null && target.isOnline()) {
            target.kickPlayer("§cВы были добавлены в черный список сервера!");
        }
    }
    
    /**
     * Удаляет игрока из списка враждебных игроков
     * @param sender игрок, выполняющий команду
     * @param playerName имя игрока для удаления
     *
     * Removes a player from the enemy player list
     * @param sender player executing the command
     * @param playerName name of the player to remove
     *
     * Entfernt einen Spieler aus der Liste feindlicher Spieler
     * @param sender Spieler, der den Befehl ausführt
     * @param playerName Name des zu entfernenden Spielers
     */
    private void removeEnemyPlayer(Player sender, String playerName) {
        
        enemyPlayerManager.removeEnemyPlayer(playerName);
        sender.sendMessage("§aИгрок " + playerName + " удален из списка враждебных игроков!");
    }
    
    /**
     * Отображает список враждебных игроков
     * @param sender игрок, выполняющий команду
     *
     * Displays the list of enemy players
     * @param sender player executing the command
     *
     * Zeigt die Liste feindlicher Spieler an
     * @param sender Spieler, der den Befehl ausführt
     */
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
    
    /**
     * Обрабатывает команды управления игроками с ограниченным доступом
     * @param sender игрок, выполняющий команду
     * @param args аргументы команды
     *
     * Handles restricted player management commands
     * @param sender player executing the command
     * @param args command arguments
     *
     * Verarbeitet Befehle zur Verwaltung von Spielern mit eingeschränktem Zugriff
     * @param sender Spieler, der den Befehl ausführt
     * @param args Befehlsargumente
     */
    private void handleRestrictedPlayers(Player sender, String[] args) {
        String action = args[1].toLowerCase();
        
        switch (action) {
            case "add":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /enemy restricted add <игрок>");
                    return;
                }
                
                sender.sendMessage("§eДобавление игроков с ограниченным доступом требует ручного изменения конфигурации.");
                break;
                
            case "remove":
                if (args.length < 3) {
                    sender.sendMessage("§cИспользование: /enemy restricted remove <игрок>");
                    return;
                }
                
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
    
    /**
     * Отображает справочную информацию по команде
     * @param sender игрок, которому отправляется справка
     *
     * Displays help information for the command
     * @param sender player to send help to
     *
     * Zeigt Hilfsinformationen für den Befehl an
     * @param sender Spieler, dem die Hilfe gesendet wird
     */
    private void sendHelp(Player sender) {
        sender.sendMessage("§e=== Команды управления враждебными игроками ===");
        sender.sendMessage("§7/enemy add <игрок> §f- Добавить враждебного игрока");
        sender.sendMessage("§7/enemy remove <игрок> §f- Удалить враждебного игрока");
        sender.sendMessage("§7/enemy list §f- Показать список враждебных игроков");
        sender.sendMessage("§7/enemy restricted list §f- Показать список игроков с ограниченным доступом");
        sender.sendMessage("§7Примечание: §fИзменение списка игроков с ограниченным доступом требует ручного изменения config.yml");
    }
    
    /**
     * Обрабатывает автозавершение команды
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param alias псевдоним команды
     * @param args аргументы команды
     * @return список возможных завершений
     *
     * Handles command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     *
     * Verarbeitet die Befehls-Tab-Vervollständigung
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param alias Befehlsalias
     * @param args Befehlsargumente
     * @return Liste möglicher Vervollständigungen
     */
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