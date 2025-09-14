package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import com.megacreative.services.CodeCompiler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

public class PlayCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public PlayCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        if (args.length > 0) {
            switch (args[0].toLowerCase()) {
                case "switch", "world" -> {
                    CreativeWorld currentWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
                    if (currentWorld != null && currentWorld.getPairedWorldId() != null) {
                        if (plugin.getBlockPlacementHandler().isInDevWorld(player)) {
                            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
                        }
                        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
                        plugin.getWorldManager().switchToPlayWorld(player, currentWorld.getId());
                        player.getInventory().clear();
                        return true;
                    }
                }
            }
        }
        
        CreativeWorld creativeWorld = plugin.getWorldManager().findCreativeWorldByBukkit(player.getWorld());
        
        // If we can't find the world directly, try to find it by ID from the world name
        if (creativeWorld == null) {
            String worldName = player.getWorld().getName();
            if (worldName.startsWith("megacreative_")) {
                // Extract world ID from the world name
                String worldId = worldName.replace("megacreative_", "");
                // Remove suffixes for dual world architecture
                worldId = worldId.replace("-code", "").replace("-world", "").replace("_dev", "");
                creativeWorld = plugin.getWorldManager().getWorld(worldId);
            }
        }
        
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            return true;
        }
        
        World currentWorld = player.getWorld();
        if (currentWorld.getName().contains("-code")) {
            CodeCompiler codeCompiler = plugin.getServiceRegistry().getCodeCompiler();
            if (codeCompiler != null) {
                try {
                    List<String> codeStrings = codeCompiler.compileWorldToCodeStrings(currentWorld);
                    String worldId = currentWorld.getName().replace("megacreative_", "").replace("-code", "");
                    codeCompiler.saveCompiledCode(worldId, codeStrings);
                    player.sendMessage("§aCode compiled successfully!");
                } catch (Exception e) {
                    player.sendMessage("§cCode compilation error: " + e.getMessage());
                    plugin.getLogger().severe("Failed to compile world code: " + e.getMessage());
                }
            }
        }
        
        if (plugin.getBlockPlacementHandler().isInDevWorld(player)) {
            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
        }
        
        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
        
        plugin.getWorldManager().switchToPlayWorld(player, creativeWorld.getId());
        
        player.getInventory().clear();
        
        return true;
    }
}