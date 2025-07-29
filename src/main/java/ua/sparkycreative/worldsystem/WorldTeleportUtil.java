package ua.sparkycreative.worldsystem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldTeleportUtil {
    public static void teleportToWorld(Player player, String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            player.sendMessage("§cМир не найден: " + worldName);
            return;
        }
        Location spawn = new Location(world, 0.5, world.getHighestBlockYAt(0, 0) + 1, 0.5);
        player.teleport(spawn);
        player.sendMessage("§aТелепортировано в мир: " + worldName);
    }
} 