package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.events.CodeBlockPlacedEvent;
import com.megacreative.coding.events.CodeBlockBrokenEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.Color;

import java.util.logging.Logger;

/**
 * ConnectionVisualizer handles visual effects for block connections
 * Listens to CodeBlockPlacedEvent and CodeBlockBrokenEvent to show visual feedback
 */
public class ConnectionVisualizer implements Listener {
    
    private static final Logger log = Logger.getLogger(ConnectionVisualizer.class.getName());
    
    private final MegaCreative plugin;
    
    public ConnectionVisualizer(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Handles code block placement events to show visual feedback
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        Player player = event.getPlayer();
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        // Add visual feedback for block placement
        player.spawnParticle(Particle.VILLAGER_HAPPY, location.add(0.5, 1.0, 0.5), 5, 0.2, 0.2, 0.2, 0.1);
        player.playSound(location, org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.8f, 1.5f);
    }
    
    /**
     * Handles code block broken events to show visual feedback
     */
    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        Player player = event.getPlayer();
        Location location = event.getLocation();
        
        // Add visual effect for block removal
        Location effectLoc = location.add(0.5, 0.5, 0.5);
        player.spawnParticle(Particle.CLOUD, effectLoc, 8, 0.3, 0.3, 0.3, 0.1);
        player.playSound(location, org.bukkit.Sound.BLOCK_NOTE_BLOCK_BASEDRUM, 0.8f, 0.8f);
    }
    
    /**
     * 🎆 ENHANCED: Adds visual effects for block connections
     * Implements reference system-style: visual code construction with feedback
     */
    public void addConnectionEffect(Location from, Location to) {
        // Create a beam of particles between connected blocks
        org.bukkit.World world = from.getWorld();
        
        // Calculate direction and distance
        double dx = to.getX() - from.getX();
        double dy = to.getY() - from.getY();
        double dz = to.getZ() - from.getZ();
        double distance = Math.sqrt(dx*dx + dy*dy + dz*dz);
        
        if (distance > 0) {
            // Normalize direction
            dx /= distance;
            dy /= distance;
            dz /= distance;
            
            // Create particle beam
            for (int i = 0; i < distance * 2; i++) {
                Location particleLoc = from.clone().add(
                    dx * i * 0.5, 
                    dy * i * 0.5, 
                    dz * i * 0.5
                );
                
                world.spawnParticle(Particle.REDSTONE, particleLoc, 1, 0, 0, 0, 0,
                    new Particle.DustOptions(Color.fromRGB(0, 255, 255), 1.0f));
            }
        }
    }
}