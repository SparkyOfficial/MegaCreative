package com.megacreative.commands;

import com.megacreative.MegaCreative;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand implements CommandExecutor {
    private final MegaCreative plugin;

    public DebugCommand(MegaCreative plugin) {
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
            // Переключаем отладку
            plugin.getScriptDebugger().toggleDebug(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on":
            case "enable":
                if (!plugin.getScriptDebugger().isDebugEnabled(player)) {
                    plugin.getScriptDebugger().toggleDebug(player);
                } else {
                    player.sendMessage("§eОтладка уже включена!");
                }
                break;
            case "off":
            case "disable":
                if (plugin.getScriptDebugger().isDebugEnabled(player)) {
                    plugin.getScriptDebugger().toggleDebug(player);
                } else {
                    player.sendMessage("§eОтладка уже выключена!");
                }
                break;
            case "stats":
                plugin.getScriptDebugger().showDebugStats(player);
                break;
            case "status":
                boolean enabled = plugin.getScriptDebugger().isDebugEnabled(player);
                player.sendMessage("§7Статус отладки: " + (enabled ? "§aВключена" : "§cВыключена"));
                break;
            case "help":
                showHelp(player);
                break;
            default:
                player.sendMessage("§cНеизвестная подкоманда: " + subCommand);
                showHelp(player);
                break;
        }

        return true;
    }

    private void showHelp(Player player) {
        player.sendMessage("§e=== Отладка скриптов ===");
        player.sendMessage("§7/debug §8- переключить отладку");
        player.sendMessage("§7/debug on §8- включить отладку");
        player.sendMessage("§7/debug off §8- выключить отладку");
        player.sendMessage("§7/debug stats §8- показать статистику");
        player.sendMessage("§7/debug status §8- показать статус");
        player.sendMessage("§7/debug help §8- показать эту справку");
        player.sendMessage("§7");
        player.sendMessage("§7При включенной отладке вы увидите:");
        player.sendMessage("§7- Эффекты частиц вокруг выполняющихся блоков");
        player.sendMessage("§7- Сообщения о выполнении каждого блока");
        player.sendMessage("§7- Параметры блоков при выполнении");
        player.sendMessage("§7- Результаты условий (истина/ложь)");
        player.sendMessage("§7- Статистику выполнения скриптов");
    }
} 