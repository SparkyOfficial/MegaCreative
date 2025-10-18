package com.megacreative.commands;

import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.coding.groups.AdvancedBlockGroup; 
import com.megacreative.core.ServiceRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Обработчик команд для функциональности группировки блоков
 * Использование: /code group <подкоманда> [аргументы...]
 *
 * Command handler for block grouping functionality
 * Usage: /code group <subcommand> [args...]
 *
 * Befehlshandler für die Blockgruppierungsfunktionalität
 * Verwendung: /code group <Unterbefehl> [Argumente...]
 */
public class GroupCommand implements CommandExecutor, TabCompleter {
    
    private final ServiceRegistry serviceRegistry;
    
    /**
     * Инициализирует обработчик команд группировки
     * @param serviceRegistry реестр сервисов
     *
     * Initializes the group command handler
     * @param serviceRegistry service registry
     *
     * Initialisiert den Gruppenbefehlshandler
     * @param serviceRegistry Serviceregister
     */
    public GroupCommand(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
    /**
     * Обрабатывает выполнение команды группировки
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param label метка команды
     * @param args аргументы команды
     * @return true если команда выполнена успешно
     *
     * Handles group command execution
     * @param sender command sender
     * @param command executed command
     * @param label command label
     * @param args command arguments
     * @return true if command executed successfully
     *
     * Verarbeitet die Ausführung des Gruppenbefehls
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param label Befehlsbezeichnung
     * @param args Befehlsargumente
     * @return true, wenn der Befehl erfolgreich ausgeführt wurde
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }
        
        BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "help" -> showHelp(player);
            case "select", "start" -> groupManager.startGroupSelection(player);
            case "create" -> {
                String groupName = args.length > 1 ? String.join(" ", Arrays.copyOfRange(args, 1, args.length)) : null;
                groupManager.createGroupFromSelection(player, groupName);
            }
            case "collapse" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /code group collapse <group_name>");
                    return true;
                }
                String groupName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                groupManager.collapseGroup(player, groupName);
            }
            case "expand" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /code group expand <group_name>");
                    return true;
                }
                String groupName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                groupManager.expandGroup(player, groupName);
            }
            case "delete", "remove" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /code group delete <group_name>");
                    return true;
                }
                String groupName = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                groupManager.deleteGroup(player, groupName);
            }
            case "list" -> groupManager.listGroups(player);
            case "cancel" -> groupManager.cancelSelection(player);
            default -> {
                player.sendMessage("§cUnknown subcommand: " + subCommand);
                showHelp(player);
            }
        }
        
        return true;
    }
    
    /**
     * Обрабатывает автозавершение команды группировки
     * @param sender отправитель команды
     * @param command выполняемая команда
     * @param alias псевдоним команды
     * @param args аргументы команды
     * @return список возможных завершений
     *
     * Handles group command tab completion
     * @param sender command sender
     * @param command executed command
     * @param alias command alias
     * @param args command arguments
     * @return list of possible completions
     *
     * Verarbeitet die Gruppenbefehls-Tab-Vervollständigung
     * @param sender Befehlsabsender
     * @param command ausgeführter Befehl
     * @param alias Befehlsalias
     * @param args Befehlsargumente
     * @return Liste möglicher Vervollständigungen
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("help", "select", "create", "collapse", "expand", "delete", "list", "cancel");
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("collapse".equals(subCommand) || "expand".equals(subCommand) || "delete".equals(subCommand)) {
                
                BlockGroupManager groupManager = serviceRegistry.getService(BlockGroupManager.class);
                if (groupManager != null) {
                    List<String> groupNames = new ArrayList<>();
                    
                    
                    List<AdvancedBlockGroup> advancedGroups = groupManager.getPlayerAdvancedGroups(player);
                    for (AdvancedBlockGroup group : advancedGroups) {
                        groupNames.add(group.getName());
                    }
                    
                    return groupNames;
                }
                
                return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
    }
    
    /**
     * Отображает справочную информацию по команде группировки
     * @param player игрок, которому отправляется справка
     *
     * Displays help information for the group command
     * @param player player to send help to
     *
     * Zeigt Hilfsinformationen für den Gruppenbefehl an
     * @param player Spieler, dem die Hilfe gesendet wird
     */
    private void showHelp(Player player) {
        player.sendMessage("§6=== Block Group Commands ===");
        player.sendMessage("§f/code group select §7- Start selecting blocks for grouping");
        player.sendMessage("§f/code group create [name] §7- Create group from selection");
        player.sendMessage("§f/code group collapse <name> §7- Collapse a group");
        player.sendMessage("§f/code group expand <name> §7- Expand a collapsed group");
        player.sendMessage("§f/code group delete <name> §7- Delete a group");
        player.sendMessage("§f/code group list §7- List all your groups");
        player.sendMessage("§f/code group cancel §7- Cancel current selection");
        player.sendMessage("");
        player.sendMessage("§7Groups allow you to organize related code blocks");
        player.sendMessage("§7and collapse them for better visual organization.");
    }
}