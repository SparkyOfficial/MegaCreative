package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PlayCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public PlayCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        // 🎆 ENHANCED: Check for dual world switching support
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "switch", "world" -> {
                    // Find current world and switch to its play version
                    CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
                    if (currentWorld != null && currentWorld.isPaired()) {
                        plugin.getWorldManager().switchToPlayWorld(player, currentWorld.getId());
                        return true;
                    }
                    // Fall through to normal play mode
                }
            }
        }
        
        // Найти мир игрока по его текущему местоположению
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        if (creativeWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        // 🎆 UNIFIED: Use centralized world switching method
        plugin.getWorldManager().switchToPlayWorld(player, creativeWorld.getId());
        
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
