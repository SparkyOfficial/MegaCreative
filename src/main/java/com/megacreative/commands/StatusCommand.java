package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StatusCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public StatusCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        World currentWorld = player.getWorld();
        CreativeWorld creativeWorld = findCreativeWorld(currentWorld);
        
        if (creativeWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        WorldMode mode = creativeWorld.getMode();
        boolean codeEnabled = mode.isCodeEnabled();
        
        player.sendMessage("§6=== Статус мира ===");
        player.sendMessage("§7Мир: §f" + creativeWorld.getName());
        player.sendMessage("§7Режим: §f" + mode.getDisplayName());
        player.sendMessage("§7Код: " + (codeEnabled ? "§a✅ ВКЛЮЧЕН" : "§c❌ ВЫКЛЮЧЕН"));
        player.sendMessage("§7Скриптов: §f" + creativeWorld.getScripts().size());
        
        if (codeEnabled) {
            player.sendMessage("§aСкрипты будут выполняться при событиях");
        } else {
            player.sendMessage("§cСкрипты не будут выполняться");
        }
        
        return true;
    }
    
    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        String worldName = bukkitWorld.getName();
        
        // 🔧 FIX: Remove prefix and ALL possible suffixes for dual world architecture
        if (worldName.startsWith("megacreative_")) {
            String id = worldName.replace("megacreative_", "")
                                  .replace("-code", "")    // New dev world suffix
                                  .replace("-world", "")   // New play world suffix  
                                  .replace("_dev", "");    // Legacy compatibility
            return plugin.getWorldManager().getWorld(id);
        }
        return null;
    }
} 