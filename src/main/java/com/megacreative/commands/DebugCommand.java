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
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length == 0) {
            showHelp(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "on":
            case "enable":
                plugin.getScriptDebugger().toggleDebug(player);
                break;
                
            case "off":
            case "disable":
                plugin.getScriptDebugger().toggleDebug(player);
                break;
                
            case "toggle":
                plugin.getScriptDebugger().toggleDebug(player);
                break;
                
            case "stats":
            case "statistics":
                plugin.getScriptDebugger().showDebugStats(player);
                break;
                
            case "step":
                plugin.getScriptDebugger().stepExecution(player);
                break;
                
            case "vars":
            case "variables":
                showVariables(player);
                break;
                
            case "clear":
                clearDebugData(player);
                break;
                
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§e=== Отладчик скриптов ===");
        player.sendMessage("§7Команды:");
        player.sendMessage("§f/debug on §7- Включить отладку");
        player.sendMessage("§f/debug off §7- Отключить отладку");
        player.sendMessage("§f/debug toggle §7- Переключить отладку");
        player.sendMessage("§f/debug stats §7- Показать статистику");
        player.sendMessage("§f/debug step §7- Пошаговое выполнение");
        player.sendMessage("§f/debug vars §7- Показать переменные");
        player.sendMessage("§f/debug clear §7- Очистить данные отладки");
        player.sendMessage("§f/debug help §7- Показать эту справку");
    }
    
    private void showVariables(Player player) {
        // TODO: Реализовать показ переменных из ExecutionContext
        player.sendMessage("§e📊 Переменные в контексте:");
        player.sendMessage("§7Функция пока не реализована");
    }
    
    private void clearDebugData(Player player) {
        // TODO: Реализовать очистку данных отладки
        player.sendMessage("§a✓ Данные отладки очищены");
    }
} 