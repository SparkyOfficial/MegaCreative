package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Команда для создания миров
 */
public class CreateWorldCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public CreateWorldCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cЭта команда только для игроков!");
            return true;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 1) {
            player.sendMessage("§cИспользование: /create <тип_мира> [имя_мира]");
            player.sendMessage("§7Доступные типы: survival, flat, void, ocean, nether, end");
            return true;
        }
        
        String worldTypeStr = args[0].toLowerCase();
        String worldName = args.length > 1 ? args[1] : generateWorldName(player);
        
        CreativeWorldType worldType = parseWorldType(worldTypeStr);
        if (worldType == null) {
            player.sendMessage("§cНеизвестный тип мира: " + worldTypeStr);
            player.sendMessage("§7Доступные типы: survival, flat, void, ocean, nether, end");
            return true;
        }
        
        // Создаем мир
        plugin.getWorldManager().createWorld(player, worldName, worldType);
        
        return true;
    }
    
    /**
     * Парсит тип мира из строки
     */
    private CreativeWorldType parseWorldType(String typeStr) {
        return switch (typeStr.toLowerCase()) {
            case "survival", "normal", "default" -> CreativeWorldType.SURVIVAL;
            case "flat" -> CreativeWorldType.FLAT;
            case "void", "empty" -> CreativeWorldType.VOID;
            case "ocean" -> CreativeWorldType.OCEAN;
            case "nether", "hell" -> CreativeWorldType.NETHER;
            case "end" -> CreativeWorldType.END;
            default -> null;
        };
    }
    
    /**
     * Генерирует имя мира для игрока
     */
    private String generateWorldName(Player player) {
        String baseName = player.getName() + "_мир";
        int counter = 1;
        String worldName = baseName;
        
        while (plugin.getWorldManager().getWorldByName(worldName) != null) {
            worldName = baseName + "_" + counter;
            counter++;
        }
        
        return worldName;
    }
} 