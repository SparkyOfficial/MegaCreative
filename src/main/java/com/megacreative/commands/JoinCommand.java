package com.megacreative.commands;

import com.megacreative.MegaCreative;
import com.megacreative.models.CreativeWorld;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class JoinCommand implements CommandExecutor {
    
    private final MegaCreative plugin;
    
    public JoinCommand(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cЭта команда доступна только игрокам!");
            return true;
        }
        
        if (args.length == 0) {
            player.sendMessage("§cИспользование: /join <ID>");
            return true;
        }
        
        String worldId = args[0];
        CreativeWorld creativeWorld = plugin.getWorldManager().getWorld(worldId);
        
        if (creativeWorld == null) {
            player.sendMessage("§cМир с ID §f" + worldId + " §cне найден!");
            return true;
        }
        
        // Проверка доступа к приватному миру
        if (creativeWorld.isPrivate() && !creativeWorld.isOwner(player) && 
            !creativeWorld.isTrustedBuilder(player) && !creativeWorld.isTrustedCoder(player)) {
            player.sendMessage("§cЭтот мир приватный!");
            return true;
        }
        
        // Загрузка мира если не загружен
        World bukkitWorld = Bukkit.getWorld(creativeWorld.getWorldName());
        if (bukkitWorld == null) {
            player.sendMessage("§eЗагрузка мира...");
            // Здесь можно добавить логику загрузки мира
            bukkitWorld = Bukkit.getWorld(creativeWorld.getWorldName());
            if (bukkitWorld == null) {
                player.sendMessage("§cОшибка загрузки мира!");
                return true;
            }
        }
        
        // Телепортация
        player.teleport(bukkitWorld.getSpawnLocation());
        creativeWorld.addOnlinePlayer(player.getUniqueId());
        
        player.sendMessage("§aВы телепортированы в мир §f" + creativeWorld.getName() + " §7(ID: " + worldId + ")");
        
        return true;
    }
}
