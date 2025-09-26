package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.AutoConnectionManager;
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
 *
 * Stellt Codeblock-Verbindungen beim Laden einer Welt wieder her
 * Hört auf WorldLoadEvent und baut Verbindungen für alle Codeblöcke in der Welt wieder auf
 */
public class WorldCodeRestorer implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(WorldCodeRestorer.class.getName());
    
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
            LOGGER.info("Restoring code connections for world: " + world.getName());
            
            try {
                // Rebuild all connections for blocks in this world
                // In the new architecture, this would involve coordinating with BlockLinker and BlockHierarchyManager
                // For now, we'll use the existing AutoConnectionManager for compatibility
                if (serviceRegistry != null) {
                    AutoConnectionManager autoConnectionManager = serviceRegistry.getAutoConnectionManager();
                    if (autoConnectionManager != null) {
                        autoConnectionManager.rebuildWorldConnections(world);
                    }
                }
                LOGGER.info("Successfully restored code connections for world: " + world.getName());
            } catch (Exception e) {
                LOGGER.severe("Failed to restore code connections for world " + world.getName() + ": " + e.getMessage());
                e.printStackTrace();
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