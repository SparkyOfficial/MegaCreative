package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import java.util.logging.Logger;

/**
 * Restores code block connections when a world is loaded
 * Listens to WorldLoadEvent and rebuilds connections for all code blocks in the world
 *
 * Восстанавливает соединения кодовых блоков при загрузке мира
 * Слушает события WorldLoadEvent и восстанавливает соединения для всех кодовых блоков в мире
 * Hört auf WorldLoadEvent und baut Verbindungen für alle Codeblöcke in der Welt wieder auf
 */
public class WorldCodeRestorer implements Listener {
    
    private final MegaCreative plugin;
    private final ServiceRegistry serviceRegistry;
    
    public WorldCodeRestorer(MegaCreative plugin) {
        this.plugin = plugin;
        this.serviceRegistry = plugin.getServiceRegistry();
    }
    
    /**
     * Handles world loading and restores code block connections
     */
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        
        // Check if this is a development world
        if (isDevWorld(world)) {
            try {
                // In the new architecture, connection restoration is handled by BlockLinker and BlockHierarchyManager
                // These services listen to events and manage connections automatically
                // For now, we'll log that the world was loaded
                plugin.getLogger().info("Development world loaded: " + world.getName() + " - connection restoration handled by services");
            } catch (Exception e) {
                // Log the error instead of silent handling
                plugin.getLogger().warning("Error handling world load for " + world.getName() + ": " + e.getMessage());
            }
        }
    }
    
    /**
     * Checks if a world is a development world
     */
    private boolean isDevWorld(World world) {
        String worldName = world.getName();
        return worldName.contains("dev") || worldName.contains("Dev") || 
               worldName.contains("разработка") || worldName.contains("Разработка") ||
               worldName.contains("creative") || worldName.contains("Creative") ||
               worldName.contains("-code") || worldName.endsWith("-code") || 
               worldName.contains("_code") || worldName.endsWith("_dev") ||
               worldName.contains("megacreative_");
    }
}