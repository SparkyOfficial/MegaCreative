package com.megacreative.gui.coding;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeBlock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;

/**
 * Utility class to update signs when code block actions are changed
 * 
 * @author Андрій Будильников
 */
public class SignUpdater {
    private final MegaCreative plugin;
    
    public SignUpdater(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Updates the sign above a code block with the current action information
     * 
     * @param blockLocation The location of the code block
     * @param codeBlock The code block with action information
     */
    public void updateSign(Location blockLocation, CodeBlock codeBlock) {
        try {
            // Find the sign above the block
            Location signLocation = blockLocation.clone().add(0, 1, 0);
            Block signBlock = signLocation.getBlock();
            
            // Check if there's a sign above the block
            if (isSign(signBlock)) {
                Sign sign = (Sign) signBlock.getState();
                
                // Clear the sign
                for (int i = 0; i < 4; i++) {
                    sign.setLine(i, "");
                }
                
                // Update the sign with code block information
                updateSignContent(sign, codeBlock);
                
                // Apply changes
                sign.update();
            } else {
                // If there's no sign above, try to create one
                createSignAbove(blockLocation, codeBlock);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating sign for code block at " + blockLocation + ": " + e.getMessage());
        }
    }
    
    /**
     * Updates the content of a sign with code block information
     * 
     * @param sign The sign to update
     * @param codeBlock The code block with information
     */
    private void updateSignContent(Sign sign, CodeBlock codeBlock) {
        String action = codeBlock.getAction();
        String materialName = codeBlock.getMaterialName();
        
        // First line - block type
        if (materialName != null) {
            Material material = Material.getMaterial(materialName);
            if (material != null) {
                sign.setLine(0, getBlockTypeDisplayName(material));
            }
        }
        
        // Second line - action
        if (action != null && !action.isEmpty() && !action.equals("NOT_SET")) {
            sign.setLine(1, ChatColor.GREEN + action);
        } else {
            sign.setLine(1, ChatColor.RED + "Не настроен");
        }
        
        // Third line - additional info
        if (codeBlock.getParameters() != null && !codeBlock.getParameters().isEmpty()) {
            sign.setLine(2, ChatColor.YELLOW + "Параметры: " + codeBlock.getParameters().size());
        } else {
            sign.setLine(2, "");
        }
        
        // Fourth line - block ID or status
        sign.setLine(3, ChatColor.GRAY + "ID: " + codeBlock.getId().toString().substring(0, 8));
    }
    
    /**
     * Gets a display name for a block type
     * 
     * @param material The material of the block
     * @return Display name for the block type
     */
    private String getBlockTypeDisplayName(Material material) {
        switch (material) {
            case DIAMOND_BLOCK:
                return ChatColor.AQUA + "[Событие]";
            case COBBLESTONE:
                return ChatColor.GRAY + "[Действие]";
            case OAK_PLANKS:
            case OBSIDIAN:
            case REDSTONE_BLOCK:
            case BRICKS:
                return ChatColor.GOLD + "[Условие]";
            case EMERALD_BLOCK:
            case END_STONE:
                return ChatColor.GREEN + "[Контроль]";
            case LAPIS_BLOCK:
            case BOOKSHELF:
                return ChatColor.BLUE + "[Функция]";
            case IRON_BLOCK:
                return ChatColor.WHITE + "[Переменная]";
            case PISTON:
            case STICKY_PISTON:
                return ChatColor.DARK_GRAY + "[Скобка]";
            default:
                return ChatColor.WHITE + "[Блок]";
        }
    }
    
    /**
     * Creates a sign above a code block
     * 
     * @param blockLocation The location of the code block
     * @param codeBlock The code block with information
     */
    private void createSignAbove(Location blockLocation, CodeBlock codeBlock) {
        try {
            Location signLocation = blockLocation.clone().add(0, 1, 0);
            Block signBlock = signLocation.getBlock();
            
            // Check if the space above is empty
            if (signBlock.getType().isAir()) {
                // Determine the direction for the sign based on block face
                // For simplicity, we'll use a wall sign on the north face
                signBlock.setType(Material.OAK_WALL_SIGN);
                
                BlockData blockData = signBlock.getBlockData();
                if (blockData instanceof WallSign) {
                    WallSign wallSign = (WallSign) blockData;
                    wallSign.setFacing(org.bukkit.block.BlockFace.NORTH);
                    signBlock.setBlockData(wallSign);
                }
                
                // Update the sign content
                if (signBlock.getState() instanceof Sign) {
                    Sign sign = (Sign) signBlock.getState();
                    updateSignContent(sign, codeBlock);
                    sign.update();
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error creating sign above code block at " + blockLocation + ": " + e.getMessage());
        }
    }
    
    /**
     * Checks if a block is a sign
     * 
     * @param block The block to check
     * @return true if the block is a sign, false otherwise
     */
    private boolean isSign(Block block) {
        if (block == null) return false;
        
        Material type = block.getType();
        return type == Material.OAK_SIGN || 
               type == Material.OAK_WALL_SIGN ||
               type == Material.SPRUCE_SIGN ||
               type == Material.SPRUCE_WALL_SIGN ||
               type == Material.BIRCH_SIGN ||
               type == Material.BIRCH_WALL_SIGN ||
               type == Material.JUNGLE_SIGN ||
               type == Material.JUNGLE_WALL_SIGN ||
               type == Material.ACACIA_SIGN ||
               type == Material.ACACIA_WALL_SIGN ||
               type == Material.DARK_OAK_SIGN ||
               type == Material.DARK_OAK_WALL_SIGN ||
               type == Material.CRIMSON_SIGN ||
               type == Material.CRIMSON_WALL_SIGN ||
               type == Material.WARPED_SIGN ||
               type == Material.WARPED_WALL_SIGN;
    }
}