package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
import com.megacreative.core.ServiceRegistry;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.Chunk;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.Location;
import org.bukkit.Material;

import java.util.logging.Level;

/**
 * WorldLoadListener for "hydrating" code blocks when worlds load
 * Restores CodeBlock objects from physical blocks and signs in the world
 */
public class WorldLoadListener implements Listener {
    private final MegaCreative plugin;

    public WorldLoadListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onWorldLoad(WorldLoadEvent event) {
        if (event == null) return;
        
        World world = event.getWorld();
        if (world == null) {
            plugin.getLogger().warning("WorldLoadEvent received with null world!");
            return;
        }
        
        String worldName = world.getName();
        if (worldName.endsWith("_dev") || worldName.endsWith("-code")) {
            plugin.getLogger().info("Found dev world: " + worldName + ". Scheduling rehydration...");
            
            // Run on the next tick to ensure world is fully loaded
            Bukkit.getScheduler().runTask(plugin, () -> {
                try {
                    rehydrateWorld(world);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error during world rehydration for " + worldName, e);
                }
            });
        }
    }

    private void rehydrateWorld(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to rehydrate null world!");
            return;
        }

        plugin.getLogger().info("Starting rehydration for world: " + world.getName());
        
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        if (placementHandler == null) {
            plugin.getLogger().severe("BlockPlacementHandler is not initialized!");
            return;
        }

        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) {
            plugin.getLogger().severe("ServiceRegistry is not initialized!");
            return;
        }

        AutoConnectionManager connectionManager = serviceRegistry.getAutoConnectionManager();
        BlockConfigService configService = serviceRegistry.getBlockConfigService();
        
        if (connectionManager == null || configService == null) {
            plugin.getLogger().severe("Required services are not initialized!");
            return;
        }

        int blockCount = 0;
        long startTime = System.currentTimeMillis();

        try {
            // Get all loaded chunks
            Chunk[] chunks = world.getLoadedChunks();
            if (chunks == null || chunks.length == 0) {
                plugin.getLogger().info("No chunks to process in world: " + world.getName());
                return;
            }
            
            plugin.getLogger().info("Processing " + chunks.length + " chunks in world: " + world.getName());
            
            // Process each chunk
            for (Chunk chunk : chunks) {
                if (chunk == null) continue;
                
                try {
                    BlockState[] tileEntities = chunk.getTileEntities();
                    if (tileEntities == null || tileEntities.length == 0) continue;
                    
                    for (BlockState tileEntity : tileEntities) {
                        if (tileEntity instanceof Sign) {
                            processSignTileEntity((Sign) tileEntity, placementHandler, connectionManager, configService);
                            blockCount++;
                            
                            // Small delay every 10 blocks to prevent server lag
                            if (blockCount % 10 == 0) {
                                Thread.sleep(1);
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error processing chunk at " + chunk.getX() + "," + chunk.getZ(), e);
                }
            }

            // Rebuild connections between blocks
            plugin.getLogger().info("Rebuilding connections for " + blockCount + " code blocks...");
            connectionManager.rebuildWorldConnections(world);
            
            // Synchronize with BlockPlacementHandler after rehydration
            // placementHandler.synchronizeWithAutoConnection();
            
            long duration = System.currentTimeMillis() - startTime;
            plugin.getLogger().info(String.format(
                "Rehydration complete for %s. Processed %d code blocks in %.2f seconds",
                world.getName(),
                blockCount,
                duration / 1000.0
            ));
            
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Critical error during world rehydration for " + world.getName(), e);
        }
    }

    /**
     * Processes a sign tile entity to check if it's part of a code block
     */
    private void processSignTileEntity(Sign sign, 
                                     BlockPlacementHandler placementHandler,
                                     AutoConnectionManager connectionManager,
                                     BlockConfigService configService) {
        if (sign == null || placementHandler == null || connectionManager == null || configService == null) {
            return;
        }

        try {
            if (!isCodeSign(sign)) {
                return;
            }

            Block codeBlock = findAssociatedCodeBlock(sign);
            if (codeBlock == null) {
                return;
            }

            Material blockType = codeBlock.getType();
            if (!configService.isCodeBlock(blockType) && 
                blockType != Material.PISTON && 
                blockType != Material.STICKY_PISTON) {
                return;
            }

            // Recreate the CodeBlock object for this physical block
            CodeBlock recreatedBlock = placementHandler.recreateCodeBlockFromExisting(codeBlock, sign);
            
            // Add to AutoConnectionManager tracking
            if (recreatedBlock != null) {
                connectionManager.addCodeBlock(codeBlock.getLocation(), recreatedBlock);
            }
        } catch (Exception e) {
            Location loc = sign.getLocation();
            plugin.getLogger().log(Level.WARNING, 
                String.format("Error processing sign at %s: %s", 
                    loc != null ? loc.toString() : "unknown location",
                    e.getMessage()
                ), 
                e
            );
        }
    }

    /**
     * Checks if a sign is part of a code block by examining its text
     */
    private boolean isCodeSign(Sign sign) {
        // Check if this is a code sign by looking for our special markers
        String[] lines = sign.getLines();
        if (lines.length < 3) return false;
        
        // Check for our special formatting
        return lines[0].contains("============") || 
               lines[0].contains("★★★★★★★★★★★★") || // Smart signs
               lines[1].contains("§e") || // Event blocks
               lines[1].contains("§a") || // Action blocks
               lines[1].contains("§6") || // Condition/bracket blocks
               lines[2].contains("Кликните ПКМ") || 
               lines[2].contains("Клик для настройки");
    }

    private Block findAssociatedCodeBlock(Sign sign) {
        // Check all adjacent faces for a code block
        Block signBlock = sign.getBlock();
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        
        for (BlockFace face : faces) {
            Block potentialBlock = signBlock.getRelative(face.getOppositeFace());
            // Check if this is a configured code block material
            if (plugin.getServiceRegistry() != null && 
                plugin.getServiceRegistry().getBlockConfigService() != null &&
                plugin.getServiceRegistry().getBlockConfigService().isCodeBlock(potentialBlock.getType())) {
                return potentialBlock;
            }
        }
        return null;
    }
}