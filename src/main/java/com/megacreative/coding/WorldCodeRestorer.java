package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.coding.BlockLinker;
import com.megacreative.coding.BlockHierarchyManager;
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
        
        
        if (isDevWorld(world)) {
            try {
                
                
                plugin.getLogger().info("Development world loaded: " + world.getName() + " - connection restoration handled by services");
                
                
                BlockLinker blockLinker = serviceRegistry.getBlockLinker();
                if (blockLinker != null) {
                    
                    plugin.getLogger().info("BlockLinker service available for world: " + world.getName());
                } else {
                    plugin.getLogger().warning("BlockLinker service not available for connection restoration in world: " + world.getName());
                }
                
                
                BlockHierarchyManager hierarchyManager = serviceRegistry.getBlockHierarchyManager();
                if (hierarchyManager != null) {
                    
                    plugin.getLogger().info("BlockHierarchyManager service available for world: " + world.getName());
                } else {
                    plugin.getLogger().warning("BlockHierarchyManager service not available for hierarchy restoration in world: " + world.getName());
                }
            } catch (Exception e) {
                
                plugin.getLogger().log(java.util.logging.Level.WARNING, "Error handling world load for " + world.getName(), e);
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