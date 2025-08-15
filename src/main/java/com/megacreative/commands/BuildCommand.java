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

public class BuildCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public BuildCommand(MegaCreative plugin) {
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
        
        if (!creativeWorld.canEdit(player)) {
            player.sendMessage("§cУ вас нет прав на изменение этого мира!");
            return true;
        }
        
        creativeWorld.setMode(WorldMode.BUILD);
        
        if (currentWorld.getName().endsWith("_dev")) {
            World mainWorld = Bukkit.getWorld(creativeWorld.getWorldName());
            if (mainWorld != null) {
                player.teleport(mainWorld.getSpawnLocation());
            }
        }
        
        if (creativeWorld.canEdit(player)) {
            player.setGameMode(GameMode.CREATIVE);
        }
        
        player.sendMessage("§aРежим мира изменен на §f§lСТРОИТЕЛЬСТВО§a!");
        player.sendMessage("§7❌ Код отключен, скрипты не будут выполняться");
        player.sendMessage("§7Креатив для строителей");
        
        plugin.getWorldManager().saveWorld(creativeWorld);
        return true;
    }
    
    private CreativeWorld findCreativeWorld(World bukkitWorld) {
        String worldName = bukkitWorld.getName();
        if (worldName.startsWith("megacreative_")) {
            String id = worldName.replace("megacreative_", "").replace("_dev", "");
            return plugin.getWorldManager().getWorld(id);
        }
        return null;
    }
}
