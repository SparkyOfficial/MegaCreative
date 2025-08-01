package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.CodeScript;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SaveScriptCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public SaveScriptCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length < 1) {
            player.sendMessage("§cИспользование: /savescript <имя_скрипта> [template]");
            player.sendMessage("§7Добавьте 'template' для сохранения как публичный шаблон");
            return true;
        }
        
        String scriptName = args[0];
        boolean isTemplate = args.length > 1 && args[1].equalsIgnoreCase("template");
        
        // Проверяем, что игрок находится в мире разработки
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        if (creativeWorld == null) {
            player.sendMessage("§cВы должны находиться в мире разработки!");
            return true;
        }
        
        // Получаем все блоки кода из мира
        Map<Location, CodeBlock> blockCodeBlocks = plugin.getBlockPlacementHandler().getBlockCodeBlocks();
        if (blockCodeBlocks.isEmpty()) {
            player.sendMessage("§cВ мире нет блоков кода для сохранения!");
            return true;
        }
        
        // Создаем скрипт
        // Находим корневой блок (событие)
        CodeBlock rootBlock = null;
        for (CodeBlock block : blockCodeBlocks.values()) {
            if (block.getMaterial() == org.bukkit.Material.DIAMOND_BLOCK) {
                rootBlock = block;
                break;
            }
        }
        
        if (rootBlock == null) {
            player.sendMessage("§cВ мире нет блока-события (алмазный блок)!");
            return true;
        }
        
        CodeScript script = new CodeScript(scriptName, true, rootBlock);
        
        if (isTemplate) {
            // Сохраняем как публичный шаблон
            script.setTemplate(true);
            script.setAuthor(player.getName());
            plugin.getTemplateManager().saveTemplate(script);
            player.sendMessage("§a✓ Шаблон '" + scriptName + "' успешно сохранен как публичный!");
            player.sendMessage("§7Другие игроки смогут импортировать его через /templates");
        } else {
            // Сохраняем как обычный скрипт для мира
            creativeWorld.getScripts().add(script);
            plugin.getWorldManager().saveWorld(creativeWorld);
            player.sendMessage("§a✓ Скрипт '" + scriptName + "' успешно сохранен!");
        }
        
        player.sendMessage("§7Блоков в скрипте: " + blockCodeBlocks.size());
        
        return true;
    }
} 