package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.gui.ScriptEditorGUI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ScriptsCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public ScriptsCommand(MegaCreative plugin) {
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
            // Открываем улучшенный редактор скриптов
            ScriptEditorGUI.openForPlayer(player);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "list":
                showScriptList(player);
                break;
                
            case "info":
                if (args.length < 2) {
                    player.sendMessage("§cИспользование: /scripts info <имя_скрипта>");
                    return true;
                }
                showScriptInfo(player, args[1]);
                break;
                
            case "help":
            default:
                showHelp(player);
                break;
        }
        
        return true;
    }
    
    private void showScriptList(Player player) {
        var world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cВы должны находиться в творческом мире!");
            return;
        }
        
        var scripts = world.getScripts();
        if (scripts.isEmpty()) {
            player.sendMessage("§7В этом мире нет скриптов");
            return;
        }
        
        player.sendMessage("§e=== Скрипты в мире ===");
        for (var script : scripts) {
            String status = script.isEnabled() ? "§a✓" : "§c✗";
            String type = script.getType().toString();
            player.sendMessage(status + " §f" + script.getName() + " §7(" + type + ")");
        }
    }
    
    private void showScriptInfo(Player player, String scriptName) {
        var world = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (world == null) {
            player.sendMessage("§cВы должны находиться в творческом мире!");
            return;
        }
        
        var script = world.getScripts().stream()
            .filter(s -> s.getName().equalsIgnoreCase(scriptName))
            .findFirst()
            .orElse(null);
            
        if (script == null) {
            player.sendMessage("§cСкрипт '" + scriptName + "' не найден!");
            return;
        }
        
        player.sendMessage("§e=== Информация о скрипте ===");
        player.sendMessage("§7Название: §f" + script.getName());
        player.sendMessage("§7Тип: §f" + script.getType());
        player.sendMessage("§7Статус: §f" + (script.isEnabled() ? "Активен" : "Неактивен"));
        player.sendMessage("§7Автор: §f" + (script.getAuthor() != null ? script.getAuthor() : "Неизвестно"));
        if (script.getDescription() != null && !script.getDescription().isEmpty()) {
            player.sendMessage("§7Описание: §f" + script.getDescription());
        }
    }
    
    private void showHelp(Player player) {
        player.sendMessage("§e=== Управление скриптами ===");
        player.sendMessage("§7Команды:");
        player.sendMessage("§f/scripts §7- Открыть редактор скриптов");
        player.sendMessage("§f/scripts list §7- Список скриптов");
        player.sendMessage("§f/scripts info <имя> §7- Информация о скрипте");
        player.sendMessage("§f/scripts help §7- Показать эту справку");
        player.sendMessage("");
        player.sendMessage("§7В редакторе скриптов:");
        player.sendMessage("§7ЛКМ - Переименовать скрипт");
        player.sendMessage("§7ПКМ - Включить/выключить скрипт");
        player.sendMessage("§7Shift+ЛКМ - Удалить скрипт");
    }
} 