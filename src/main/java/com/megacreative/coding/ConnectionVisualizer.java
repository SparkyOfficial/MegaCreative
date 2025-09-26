package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.events.CodeBlocksConnectedEvent;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Отвечает только за визуальные эффекты, связанные с кодом.
 */
public class ConnectionVisualizer implements Listener {

    private final MegaCreative plugin;

    public ConnectionVisualizer(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlocksConnected(CodeBlocksConnectedEvent event) {
        addConnectionEffect(event.getFromLocation(), event.getToLocation());
    }

    private void addConnectionEffect(Location from, Location to) {
        if (from == null || to == null || from.getWorld() == null) return;
        
        org.bukkit.World world = from.getWorld();
        double distance = from.distance(to);
        if (distance == 0) return;
        
        org.bukkit.util.Vector vector = to.toVector().subtract(from.toVector()).normalize().multiply(0.25);
        Location current = from.clone().add(0.5, 0.5, 0.5); // Центрируем частицы

        for (double d = 0; d < distance; d += 0.25) {
            world.spawnParticle(Particle.REDSTONE, current, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.AQUA, 1.0f));
            current.add(vector);
        }
    }
}