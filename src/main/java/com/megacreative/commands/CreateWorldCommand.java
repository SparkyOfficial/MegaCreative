package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorldType;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreateWorldCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public CreateWorldCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // Check if player can create more worlds
        if (plugin.getWorldManager().getPlayerWorldCount(player) >= 5) {
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
        
        // Generate world name
        String worldName = player.getName() + "'s World " + (plugin.getWorldManager().getPlayerWorldCount(player) + 1);
        
        // Create the world
        player.sendMessage("§a⏳ Создание мира...");
        
        plugin.getWorldManager().createWorld(player, worldName, worldType);
        
        // The world creation is handled in the WorldManager
        // It will send appropriate messages to the player
        
        return true;
    }
}