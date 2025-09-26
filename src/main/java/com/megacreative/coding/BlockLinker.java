package com.megacreative.coding;

import com.megacreative.MegaCreative;
import com.megacreative.events.CodeBlockPlacedEvent;
import com.megacreative.events.CodeBlocksConnectedEvent;
import com.megacreative.worlds.DevWorldGenerator;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Handles horizontal connections between code blocks (nextBlock relationships)
 * Listens to CodeBlockPlacedEvent and establishes connections between adjacent blocks
 *
 * Обрабатывает горизонтальные соединения между кодовыми блоками (отношения nextBlock)
 * Слушает события CodeBlockPlacedEvent и устанавливает соединения между соседними блоками
 *
 * Behandelt horizontale Verbindungen zwischen Codeblöcken (nextBlock-Beziehungen)
 * Hört auf CodeBlockPlacedEvent und stellt Verbindungen zwischen benachbarten Blöcken her
 */
public class BlockLinker implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(BlockLinker.class.getName());
    
    private final MegaCreative plugin;
    private final Map<Location, CodeBlock> locationToBlock = new HashMap<>();
    
    // Shared map with other connection managers
    private Map<Location, CodeBlock> sharedLocationToBlock = new HashMap<>();
    
    public BlockLinker(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Sets the shared location to block map
     * @param sharedMap The shared map
     */
    public void setSharedLocationToBlock(Map<Location, CodeBlock> sharedMap) {
        this.sharedLocationToBlock = sharedMap;
    }
    
    /**
     * Gets all blocks in a world
     * @param world The world to get blocks from
     * @return A map of locations to code blocks in the world
     */
    public Map<Location, CodeBlock> getWorldBlocks(World world) {
        Map<Location, CodeBlock> worldBlocks = new HashMap<>();
        // First check our own map
        for (Map.Entry<Location, CodeBlock> entry : locationToBlock.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                worldBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        // Then check the shared map
        for (Map.Entry<Location, CodeBlock> entry : sharedLocationToBlock.entrySet()) {
            if (entry.getKey().getWorld().equals(world)) {
                worldBlocks.put(entry.getKey(), entry.getValue());
            }
        }
        return worldBlocks;
    }
    
    /**
     * Handles block placement and establishes horizontal connections
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        CodeBlock codeBlock = event.getCodeBlock();
        Location location = event.getLocation();
        
        // Skip connection logic for bracket blocks
        if (codeBlock.isBracket()) {
            LOGGER.fine("Skipping connection for bracket block at " + location);
            return;
        }
        
        // Add block to our tracking
        locationToBlock.put(location, codeBlock);
        
        // Connect with previous block in the same line (horizontal connection)
        Location prevLocation = getPreviousLocationInLine(location);
        if (prevLocation != null) {
            CodeBlock prevBlock = locationToBlock.get(prevLocation);
            if (prevBlock != null && !prevBlock.isBracket()) { // Skip brackets
                prevBlock.setNextBlock(codeBlock);
                LOGGER.fine("Connected horizontal: " + prevLocation + " -> " + location);
                
                // Fire event for connection visualization
                plugin.getServer().getPluginManager().callEvent(
                    new CodeBlocksConnectedEvent(prevLocation, location));
            }
        }
        
        // Connect with next block in the same line (for validation)
        Location nextLocation = getNextLocationInLine(location);
        if (nextLocation != null) {
            CodeBlock nextBlock = locationToBlock.get(nextLocation);
            if (nextBlock != null && !nextBlock.isBracket()) { // Skip brackets
                codeBlock.setNextBlock(nextBlock);
                LOGGER.fine("Connected horizontal: " + location + " -> " + nextLocation);
                
                // Fire event for connection visualization
                plugin.getServer().getPluginManager().callEvent(
                    new CodeBlocksConnectedEvent(location, nextLocation));
            }
        }
    }
    
    /**
     * Gets the previous location in the same line
     */
    private Location getPreviousLocationInLine(Location location) {
        if (location == null) return null;
        
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return null;
        
        int currentX = location.getBlockX();
        if (currentX <= 0) return null;
        
        // Return the previous block in the same line
        return new Location(location.getWorld(), currentX - 1, location.getBlockY(), location.getBlockZ()).clone();
    }
    
    /**
     * Gets the next location in the same line
     */
    private Location getNextLocationInLine(Location location) {
        if (location == null) return null;
        
        int line = DevWorldGenerator.getCodeLineFromZ(location.getBlockZ());
        if (line == -1) return null;
        
        int currentX = location.getBlockX();
        int maxBlocksPerLine = DevWorldGenerator.getBlocksPerLine();
        
        if (currentX >= maxBlocksPerLine - 1) return null;
        
        // Return the next block in the same line
        return new Location(location.getWorld(), currentX + 1, location.getBlockY(), location.getBlockZ()).clone();
    }
}