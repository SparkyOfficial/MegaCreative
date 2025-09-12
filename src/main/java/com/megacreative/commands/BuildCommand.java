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
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        CreativeWorld creativeWorld = worldManager.findCreativeWorldByBukkit(player.getWorld());
        
        if (creativeWorld == null) {
            player.sendMessage("§cВы не находитесь в мире MegaCreative!");
            return true;
        }
        
        if (!creativeWorld.canEdit(player)) {
            player.sendMessage("§cУ вас нет прав на изменение этого мира!");
            return true;
        }
        
        // Восстанавливаем "обычный" инвентарь игрока ПЕРЕД телепортацией
        plugin.getServiceRegistry().getDevInventoryManager().restorePlayerInventory(player);
        
        // 🎆 UNIFIED: Set mode and switch to build world with BUILD mode
        creativeWorld.setMode(WorldMode.BUILD);
        worldManager.switchToBuildWorld(player, creativeWorld.getId());
        
        player.sendMessage("§aРежим мира изменен на §f§lСТРОИТЕЛЬСТВО§a!");
        player.sendMessage("§7❌ Код отключен, скрипты не будут выполняться");
        player.sendMessage("§7Креатив для строителей");
        
        // После телепорта в режим BUILD инвентарь игрока должен быть очищен
        player.getInventory().clear();
        
        worldManager.saveWorld(creativeWorld);
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
            return worldManager.getWorld(id);
        }
        return null;
    }
}