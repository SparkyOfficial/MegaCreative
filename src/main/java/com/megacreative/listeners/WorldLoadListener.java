package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.CodeBlock;
import com.megacreative.core.ServiceRegistry;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

/**
 * Listens for world load events and rehydrates code blocks in the world
 * This ensures that code blocks are properly restored when worlds are loaded
 */
public class WorldLoadListener implements Listener {
    private final MegaCreative plugin;

    public WorldLoadListener(MegaCreative plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        World world = event.getWorld();
        String worldName = world.getName();
        
        
        if (worldName.contains("_dev") || worldName.contains("_creative")) {
            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                try {
                    rehydrateWorld(world);
                } catch (Exception e) {
                    plugin.getLogger().log(Level.SEVERE, "Error during world rehydration for " + worldName, e);
                }
            }, 20L); 
        }
    }

    private void rehydrateWorld(World world) {
        if (world == null) {
            plugin.getLogger().warning("Attempted to rehydrate null world!");
            return;
        }

        plugin.getLogger().info("Starting rehydration for world: " + world.getName());
        
        BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
        if (placementHandler == null) {
            plugin.getLogger().severe("BlockPlacementHandler is not initialized!");
            return;
        }

        ServiceRegistry serviceRegistry = plugin.getServiceRegistry();
        if (serviceRegistry == null) {
            plugin.getLogger().severe("ServiceRegistry is not initialized!");
            return;
        }

        BlockConfigService configService = serviceRegistry.getBlockConfigService();
        
        if (configService == null) {
            plugin.getLogger().severe("Required services are not initialized!");
            return;
        }

        
        Chunk[] chunks = world.getLoadedChunks();
        if (chunks == null || chunks.length == 0) {
            plugin.getLogger().info("No chunks to process in world: " + world.getName());
            return;
        }
        
        plugin.getLogger().info("Processing " + chunks.length + " chunks in world: " + world.getName());
        
        
        processChunksAsync(world, chunks, placementHandler, configService);
    }
    
    private void processChunksAsync(World world, Chunk[] chunks, 
                                  BlockPlacementHandler placementHandler, 
                                  BlockConfigService configService) {
        AtomicInteger chunkIndex = new AtomicInteger(0);
        AtomicInteger blockCount = new AtomicInteger(0);
        long startTime = System.currentTimeMillis();
        
        new BukkitRunnable() {
            @Override
            public void run() {
                int currentIndex = chunkIndex.getAndIncrement();
                
                
                if (currentIndex >= chunks.length) {
                    
                    long duration = System.currentTimeMillis() - startTime;
                    plugin.getLogger().info(String.format(
                        "Rehydration complete for %s. Processed %d code blocks in %.2f seconds",
                        world.getName(),
                        blockCount.get(),
                        duration / 1000.0
                    ));
                    cancel();
                    return;
                }
                
                
                Chunk chunk = chunks[currentIndex];
                if (chunk == null) return;
                
                try {
                    BlockState[] tileEntities = chunk.getTileEntities();
                    if (tileEntities == null || tileEntities.length == 0) return;
                    
                    int processedInThisChunk = 0;
                    for (BlockState tileEntity : tileEntities) {
                        if (tileEntity instanceof Sign) {
                            processSignTileEntity((Sign) tileEntity, placementHandler, configService);
                            blockCount.incrementAndGet();
                            processedInThisChunk++;
                            
                            
                            if (processedInThisChunk % 10 == 0) {
                                
                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING, "Error processing chunk at " + chunk.getX() + "," + chunk.getZ(), e);
                }
            }
        }.runTaskTimer(plugin, 1L, 1L); 
    }

    /**
     * Processes a sign tile entity to check if it's part of a code block
     */
    private void processSignTileEntity(Sign sign, 
                                     BlockPlacementHandler placementHandler,
                                     BlockConfigService configService) {
        if (sign == null || placementHandler == null || configService == null) {
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

            
            CodeBlock recreatedBlock = placementHandler.recreateCodeBlockFromExisting(codeBlock, sign);
            
            
        } catch (Exception e) {
            org.bukkit.Location loc = sign.getLocation();
            plugin.getLogger().log(Level.WARNING, 
                String.format("Error processing sign at %s: %s", 
                    loc,
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
        
        String[] lines = sign.getLines();
        if (lines.length < 3) return false;
        
        
        return lines[0].contains("============") || 
               lines[0].contains("★★★★★★★★★★★★") || 
               lines[1].contains("§e") || 
               lines[1].contains("§a") || 
               lines[1].contains("§6") || 
               lines[2].contains("Кликните ПКМ") || 
               lines[2].contains("Клик для настройки");
    }

    private Block findAssociatedCodeBlock(Sign sign) {
        
        Block signBlock = sign.getBlock();
        BlockFace[] faces = { BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST };
        
        for (BlockFace face : faces) {
            Block potentialBlock = signBlock.getRelative(face.getOppositeFace());
            
            if (plugin.getServiceRegistry() != null && 
                plugin.getServiceRegistry().getBlockConfigService() != null &&
                plugin.getServiceRegistry().getBlockConfigService().isCodeBlock(potentialBlock.getType())) {
                return potentialBlock;
            }
        }
        return null;
    }
}