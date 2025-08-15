package com.megacreative.commands;

import com.megacreative.MegaCreative;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class VisualizeCommand implements CommandExecutor {
    private final MegaCreative plugin;

    public VisualizeCommand(MegaCreative plugin) {
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
            // Переключаем визуализацию
            plugin.getBlockConnectionVisualizer().toggleVisualization(player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "on":
            case "enable":
                if (!plugin.getBlockConnectionVisualizer().isVisualizationEnabled(player)) {
                    plugin.getBlockConnectionVisualizer().toggleVisualization(player);
                } else {
                    player.sendMessage("§eВизуализация уже включена!");
                }
                break;
            case "off":
            case "disable":
                if (plugin.getBlockConnectionVisualizer().isVisualizationEnabled(player)) {
                    plugin.getBlockConnectionVisualizer().toggleVisualization(player);
                } else {
                    player.sendMessage("§eВизуализация уже выключена!");
                }
                break;
            case "status":
                boolean enabled = plugin.getBlockConnectionVisualizer().isVisualizationEnabled(player);
                player.sendMessage("§7Статус визуализации: " + (enabled ? "§aВключена" : "§cВыключена"));
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
        player.sendMessage("§e=== Визуализация соединений ===");
        player.sendMessage("§7/visualize §8- переключить визуализацию");
        player.sendMessage("§7/visualize on §8- включить визуализацию");
        player.sendMessage("§7/visualize off §8- выключить визуализацию");
        player.sendMessage("§7/visualize status §8- показать статус");
        player.sendMessage("§7/visualize help §8- показать эту справку");
        player.sendMessage("§7");
        player.sendMessage("§7Цвета соединений:");
        player.sendMessage("§bИскры §8- события (алмазные блоки)");
        player.sendMessage("§aЗеленые §8- условия (доски)");
        player.sendMessage("§cКрасные §8- действия (булыжник)");
        player.sendMessage("§5Фиолетовые §8- переменные (железо)");
        player.sendMessage("§7Серые §8- иначе (эндерняк)");
        player.sendMessage("§6Оранжевые §8- игровые действия (незерит)");
    }
} 