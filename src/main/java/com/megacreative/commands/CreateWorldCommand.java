package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateWorldCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    public CreateWorldCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Check if player can create more worlds
        if (worldManager.getPlayerWorldCount(player) >= 5) {
            player.sendMessage("§c❌ Вы уже создали максимальное количество миров (5)!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§c❌ Укажите тип мира! Доступные типы:");
            player.sendMessage("§7- survival (обычный мир)");
            player.sendMessage("§7- flat (плоский мир)");
            player.sendMessage("§7- void (пустой мир)");
            player.sendMessage("§7- ocean (океанский мир)");
            player.sendMessage("§7- nether (адский мир)");
            player.sendMessage("§7- end (краевой мир)");
            return true;
        }
        
        String typeStr = args[0].toLowerCase();
        CreativeWorldType worldType;
        
        try {
            worldType = CreativeWorldType.valueOf(typeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage("§c❌ Неизвестный тип мира: " + typeStr);
            return true;
        }
        
        // Generate world name - use provided name or generate default
        String worldName;
        if (args.length > 1) {
            // Join all remaining arguments to form the world name
            StringBuilder nameBuilder = new StringBuilder();
            for (int i = 1; i < args.length; i++) {
                if (i > 1) nameBuilder.append(" ");
                nameBuilder.append(args[i]);
            }
            worldName = nameBuilder.toString();
        } else {
            // Generate default world name
            worldName = player.getName() + "'s World " + (worldManager.getPlayerWorldCount(player) + 1);
        }
        
        // Validate world name
        if (worldName.length() < 3 || worldName.length() > 20) {
            player.sendMessage("§cНазвание мира должно содержать от 3 до 20 символов!");
            return true;
        }
        
        if (!worldName.matches("^[a-zA-Z0-9_\\sА-Яа-яЁё]+$")) {
            player.sendMessage("§cНазвание мира может содержать только буквы, цифры, пробелы и подчеркивания!");
            return true;
        }
        
        // Create the world
        player.sendMessage("§a⏳ Создание мира \"" + worldName + "\"...");
        
        worldManager.createWorld(player, worldName, worldType);
        
        // The world creation is handled in the WorldManager
        // It will send appropriate messages to the player
        
        return true;
    }
}