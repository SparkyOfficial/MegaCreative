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
            player.sendMessage("§cИспользование: /savescript <имя_скрипта>");
            return true;
        }
        
        String scriptName = args[0];
        
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
        
        // Добавляем скрипт в мир
        creativeWorld.getScripts().add(script);
        
        // Сохраняем мир
        try {
            plugin.getWorldManager().saveWorld(creativeWorld);
            player.sendMessage("§a✓ Скрипт '" + scriptName + "' успешно сохранен!");
            player.sendMessage("§7Блоков в скрипте: " + blockCodeBlocks.size());
        } catch (Exception e) {
            player.sendMessage("§cОшибка при сохранении скрипта: " + e.getMessage());
            plugin.getLogger().severe("Ошибка сохранения скрипта: " + e.getMessage());
        }
        
        return true;
    }
} 