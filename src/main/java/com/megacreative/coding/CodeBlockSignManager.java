package com.megacreative.coding;

import com.megacreative.coding.events.CodeBlockPlacedEvent;
import com.megacreative.coding.events.CodeBlockBrokenEvent;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.ChatColor;
import java.util.logging.Logger;
import java.util.UUID;

/**
 * Manages sign creation and removal for CodeBlocks
 * Listens to CodeBlockPlacedEvent and CodeBlockBrokenEvent to automatically
 * create and remove signs for code blocks
 *
 * Управляет созданием и удалением табличек для CodeBlocks
 * Слушает события CodeBlockPlacedEvent и CodeBlockBrokenEvent для автоматического
 * создания и удаления табличек для кодовых блоков
 *
 * Verwaltet die Erstellung und Entfernung von Schildern für CodeBlocks
 * Hört auf CodeBlockPlacedEvent und CodeBlockBrokenEvent, um automatisch
 * Schilder für Code-Blöcke zu erstellen und zu entfernen
 */
public class CodeBlockSignManager implements Listener {
    
    private static final Logger LOGGER = Logger.getLogger(CodeBlockSignManager.class.getName());
    
    private final BlockConfigService blockConfigService;
    
    public CodeBlockSignManager(BlockConfigService blockConfigService) {
        this.blockConfigService = blockConfigService;
    }
    
    /**
     * Creates a sign for a code block when it's placed
     */
    @EventHandler
    public void onCodeBlockPlaced(CodeBlockPlacedEvent event) {
        try {
            createSignForBlock(event.getLocation(), event.getCodeBlock());
        } catch (Exception e) {
            LOGGER.severe("Failed to create sign for code block at " + event.getLocation() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Removes a sign for a code block when it's broken
     */
    @EventHandler
    public void onCodeBlockBroken(CodeBlockBrokenEvent event) {
        try {
            removeSignFromBlock(event.getLocation());
        } catch (Exception e) {
            LOGGER.severe("Failed to remove sign for code block at " + event.getLocation() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Creates a sign for a code block
     */
    private void createSignForBlock(Location blockLocation, CodeBlock codeBlock) {
        // Find an appropriate location for the sign (above the block)
        Location signLocation = blockLocation.clone().add(0, 1, 0);
        Block signBlock = signLocation.getBlock();
        
        // Check if there's already a block above
        if (!signBlock.getType().isAir() && signBlock.getType() != Material.OAK_SIGN && signBlock.getType() != Material.OAK_WALL_SIGN) {
            // Try to find another spot
            signLocation = findAvailableSignLocation(blockLocation);
            if (signLocation == null) {
                LOGGER.warning("No available location for sign at " + blockLocation);
                return;
            }
            signBlock = signLocation.getBlock();
        }
        
        // Set the block to a sign
        if (signBlock.getType().isAir() || signBlock.getType() == Material.OAK_SIGN || signBlock.getType() == Material.OAK_WALL_SIGN) {
            signBlock.setType(Material.OAK_SIGN); // Use oak sign by default
            
            // Update the sign text
            if (signBlock.getState() instanceof Sign sign) {
                updateSignText(sign, codeBlock);
                sign.update();
            }
        }
    }
    
    /**
     * Removes a sign from a block location
     */
    private void removeSignFromBlock(Location blockLocation) {
        // Check above the block for a sign
        Location signLocation = blockLocation.clone().add(0, 1, 0);
        Block signBlock = signLocation.getBlock();
        
        // If there's a sign above, remove it
        if (signBlock.getType() == Material.OAK_SIGN || signBlock.getType() == Material.OAK_WALL_SIGN) {
            signBlock.setType(Material.AIR);
        }
    }
    
    /**
     * Updates the text on a sign based on the code block
     */
    private void updateSignText(Sign sign, CodeBlock codeBlock) {
        String[] lines = new String[4];
        
        // First line: Block type indicator
        if (codeBlock.isBracket()) {
            lines[0] = ChatColor.GOLD + "[BRACKET]";
        } else {
            lines[0] = ChatColor.BLUE + "[CODE]";
        }
        
        // Second line: Action/Event/Condition
        if (codeBlock.getAction() != null && !codeBlock.getAction().equals("NOT_SET")) {
            lines[1] = ChatColor.WHITE + truncateString(codeBlock.getAction(), 15);
        } else if (codeBlock.getEvent() != null && !codeBlock.getEvent().equals("NOT_SET")) {
            lines[1] = ChatColor.GREEN + truncateString(codeBlock.getEvent(), 15);
        } else if (codeBlock.getCondition() != null && !codeBlock.getCondition().equals("NOT_SET")) {
            lines[1] = ChatColor.YELLOW + truncateString(codeBlock.getCondition(), 15);
        } else {
            lines[1] = ChatColor.GRAY + "NOT_SET";
        }
        
        // Third line: Material
        lines[2] = ChatColor.GRAY + truncateString(codeBlock.getMaterial().name(), 15);
        
        // Fourth line: ID or status
        if (codeBlock.getId() != null) {
            String id = codeBlock.getId().toString();
            lines[3] = ChatColor.DARK_GRAY + "#" + id.substring(0, Math.min(12, id.length()));
        } else {
            lines[3] = "";
        }
        
        // Set the lines on the sign
        for (int i = 0; i < 4; i++) {
            if (lines[i] != null) {
                sign.setLine(i, lines[i]);
            }
        }
    }
    
    /**
     * Finds an available location for placing a sign near a block
     */
    private Location findAvailableSignLocation(Location blockLocation) {
        // Check adjacent locations for available space
        Location[] adjacentLocations = {
            blockLocation.clone().add(1, 0, 0),  // East
            blockLocation.clone().add(-1, 0, 0), // West
            blockLocation.clone().add(0, 0, 1),  // South
            blockLocation.clone().add(0, 0, -1), // North
            blockLocation.clone().add(0, 1, 0)   // Above (already checked, but included for completeness)
        };
        
        for (Location location : adjacentLocations) {
            Block block = location.getBlock();
            if (block.getType().isAir() || block.getType() == Material.OAK_SIGN || block.getType() == Material.OAK_WALL_SIGN) {
                return location;
            }
        }
        
        return null; // No available location found
    }
    
    /**
     * Truncates a string to a maximum length and adds ellipsis if needed
     */
    private String truncateString(String str, int maxLength) {
        if (str == null) return "";
        if (str.length() <= maxLength) return str;
        return str.substring(0, maxLength - 3) + "...";
    }
}