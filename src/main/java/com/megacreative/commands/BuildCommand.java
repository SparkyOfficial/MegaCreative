package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.interfaces.IWorldManager;
import com.megacreative.models.CreativeWorld;
import com.megacreative.models.WorldMode;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BuildCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    private final IWorldManager worldManager;
    
    public BuildCommand(MegaCreative plugin, IWorldManager worldManager) {
        this.plugin = plugin;
        this.worldManager = worldManager;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cThis command is only available to players!");
            return true;
        }
        
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        // If we can't find the world directly, try to find it by ID from the world name
        if (creativeWorld == null) {
            String worldName = player.getWorld().getName();
            if (worldName.startsWith("megacreative_")) {
                // Extract world ID from the world name
                String worldId = worldName.replace("megacreative_", "");
                // Remove suffixes for dual world architecture
                worldId = worldId.replace("-code", "").replace("-world", "").replace("_dev", "");
                creativeWorld = worldManager.getWorld(worldId);
            }
        }
        
        if (creativeWorld == null) {
            player.sendMessage("§cYou are not in a MegaCreative world!");
            return true;
        }
        
        if (!creativeWorld.canEdit(player)) {
            player.sendMessage("§cYou don't have permission to edit this world!");
            return true;
        }
        
        if (plugin.getBlockPlacementHandler().isInDevWorld(player)) {
            plugin.getServiceRegistry().getDevInventoryManager().savePlayerInventory(player);
        }
        
        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
        
        creativeWorld.setMode(com.megacreative.models.WorldMode.BUILD);
        worldManager.switchToBuildWorld(player, creativeWorld.getId());
        
        player.sendMessage("§aWorld mode changed to §f§lBUILD§a!");
        player.sendMessage("§7❌ Code disabled, scripts will not execute");
        player.sendMessage("§7Creative mode for builders");
        
        player.getInventory().clear();
        
        worldManager.saveWorld(creativeWorld);
        return true;
    }
}