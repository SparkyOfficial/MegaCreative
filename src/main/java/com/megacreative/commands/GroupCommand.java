package com.megacreative.commands;

import com.megacreative.coding.groups.BlockGroupManager;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Command handler for block grouping functionality
 * Usage: /code group <subcommand> [args...]
 */
public class GroupCommand implements CommandExecutor, TabCompleter {
    
    private final ServiceRegistry serviceRegistry;
    
    public GroupCommand(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    
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
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Arrays.asList("help", "select", "create", "collapse", "expand", "delete", "list", "cancel");
        }
        
        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if ("collapse".equals(subCommand) || "expand".equals(subCommand) || "delete".equals(subCommand)) {
                // Here we could return group names, but we'd need access to the group manager
                // For now, return empty list
                return new ArrayList<>();
            }
        }
        
        return new ArrayList<>();
    }
    
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