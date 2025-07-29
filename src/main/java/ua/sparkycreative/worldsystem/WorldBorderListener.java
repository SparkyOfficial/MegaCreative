package ua.sparkycreative.worldsystem;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class WorldBorderListener implements Listener {
    private static final int BORDER = 150; // 300x300, центр в (0,0)

    public WorldBorderListener() {
        Bukkit.getPluginManager().registerEvents(this, WorldSystemPlugin.getInstance());
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
        World world = player.getWorld();
        String name = world.getName();
        if (!name.contains("_")) return; // Только пользовательские и dev-миры
        double x = e.getTo().getX();
        double z = e.getTo().getZ();
        if (Math.abs(x) > BORDER || Math.abs(z) > BORDER) {
            double clampedX = Math.max(-BORDER + 0.5, Math.min(BORDER - 0.5, x));
            double clampedZ = Math.max(-BORDER + 0.5, Math.min(BORDER - 0.5, z));
            Location safe = new Location(world, clampedX, world.getHighestBlockYAt((int)clampedX, (int)clampedZ) + 1, clampedZ);
            player.teleport(safe);
            player.sendMessage("§cВы достигли границы мира 300x300");
        }
    }
} 