package com.megacreative.listeners;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockPlacementHandler;
import com.megacreative.coding.AutoConnectionManager;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.CodeBlock;
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
        World world = event.getWorld();
        // Check if this is a dev world (both old _dev and new -code suffixes)
        if (world.getName().endsWith("_dev") || world.getName().endsWith("-code")) {
            plugin.getLogger().info("Found dev world: " + world.getName() + ". Rehydrating code blocks...");
            // Run asynchronously to avoid blocking the main thread during world loading
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                rehydrateWorld(world);
            });
        }
    }

    private void rehydrateWorld(World world) {
        BlockPlacementHandler placementHandler = plugin.getBlockPlacementHandler();
        AutoConnectionManager connectionManager = plugin.getServiceRegistry().getAutoConnectionManager();
        BlockConfigService configService = plugin.getServiceRegistry().getBlockConfigService();
        int blockCount = 0;

        try {
            // Iterate through all loaded chunks in the world
            for (Chunk chunk : world.getLoadedChunks()) {
                // Check all block states (tile entities) in the chunk - this is much more efficient
                for (BlockState tileEntity : chunk.getTileEntities()) {
                    // Look for signs that might be code block signs
                    if (tileEntity instanceof Sign) {
                        Sign sign = (Sign) tileEntity;
                        // Check if this is a code sign
                        if (isCodeSign(sign)) {
                            Block codeBlock = findAssociatedCodeBlock(sign);
                            if (codeBlock != null && (configService.isCodeBlock(codeBlock.getType()) || 
                                    codeBlock.getType() == Material.PISTON || codeBlock.getType() == Material.STICKY_PISTON)) {
                                // Recreate the CodeBlock object for this physical block
                                placementHandler.recreateCodeBlockFromExisting(codeBlock, sign);
                                
                                // Add to AutoConnectionManager tracking
                                CodeBlock recreatedBlock = placementHandler.getCodeBlock(codeBlock.getLocation());
                                if (recreatedBlock != null) {
                                    connectionManager.addCodeBlock(codeBlock.getLocation(), recreatedBlock);
                                    blockCount++;
                                }
                            }
                        }
                    }
                }
            }

            // After all blocks are loaded, rebuild connections between them
            // Make variables effectively final for lambda expression
            final AutoConnectionManager finalConnectionManager = connectionManager;
            final World finalWorld = world;
            final int finalBlockCount = blockCount;
            Bukkit.getScheduler().runTask(plugin, () -> {
                finalConnectionManager.rebuildWorldConnections(finalWorld);
                plugin.getLogger().info("Rehydration complete. Loaded " + finalBlockCount + " code blocks for world " + finalWorld.getName());
                
                // 🔧 FIX: Synchronize with BlockPlacementHandler after rehydration
                placementHandler.synchronizeWithAutoConnection();
            });
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error during world rehydration for " + world.getName(), e);
        }
    }

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
            if (plugin.getServiceRegistry().getBlockConfigService().isCodeBlock(potentialBlock.getType())) {
                return potentialBlock;
            }
        }
        return null;
    }
}