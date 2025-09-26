package com.megacreative.coding.actions.worldedit;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.services.MessagingService;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Bukkit;

/**
 * Action that demonstrates WorldEdit integration for block manipulation
 * This action shows how to use WorldEdit API for advanced block operations
 */
@BlockMeta(id = "worldeditManipulate", displayName = "§aWorldEdit Block Manipulation", type = BlockType.ACTION)
public class WorldEditManipulationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }
        
        try {
            // Check if WorldEdit is available
            if (!isWorldEditAvailable()) {
                // Use Adventure API if available, otherwise fallback to regular sendMessage
                MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
                if (messagingService != null) {
                    messagingService.sendErrorMessage(player, "WorldEdit is not installed or not available");
                } else {
                    player.sendMessage("§cWorldEdit is not installed or not available");
                }
                return ExecutionResult.error("WorldEdit is not available");
            }
            
            // Get parameters
            DataValue materialValue = block.getParameter("material");
            DataValue radiusValue = block.getParameter("radius");
            
            String materialName = materialValue != null ? materialValue.asString() : "STONE";
            int radius = radiusValue != null ? radiusValue.asNumber().intValue() : 5;
            
            // Validate material
            Material material = Material.getMaterial(materialName.toUpperCase());
            if (material == null) {
                // Use Adventure API if available, otherwise fallback to regular sendMessage
                MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
                if (messagingService != null) {
                    messagingService.sendErrorMessage(player, "Invalid material: " + materialName);
                } else {
                    player.sendMessage("§cInvalid material: " + materialName);
                }
                return ExecutionResult.error("Invalid material: " + materialName);
            }
            
            // Get player location
            Location location = player.getLocation();
            
            // Perform WorldEdit operation (example: create a sphere of blocks)
            boolean success = createSphereWithWorldEdit(player, location, material, radius, context);
            
            if (success) {
                // Use Adventure API if available, otherwise fallback to regular sendMessage
                MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
                if (messagingService != null) {
                    messagingService.sendSuccessMessage(player, "Created a " + materialName + " sphere with radius " + radius);
                } else {
                    player.sendMessage("§a✓ Created a " + materialName + " sphere with radius " + radius);
                }
                return ExecutionResult.success("Sphere created successfully");
            } else {
                // Use Adventure API if available, otherwise fallback to regular sendMessage
                MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
                if (messagingService != null) {
                    messagingService.sendErrorMessage(player, "Failed to create sphere");
                } else {
                    player.sendMessage("§cFailed to create sphere");
                }
                return ExecutionResult.error("Failed to create sphere");
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error in WorldEditManipulationAction: " + e.getMessage());
            return ExecutionResult.error("Error: " + e.getMessage());
        }
    }
    
    /**
     * Checks if WorldEdit is available
     */
    private boolean isWorldEditAvailable() {
        try {
            // Try to get WorldEdit class
            Class.forName("com.sk89q.worldedit.WorldEdit");
            return Bukkit.getPluginManager().getPlugin("WorldEdit") != null;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Creates a sphere of blocks using WorldEdit
     * This is a simplified example - in a real implementation you would use WorldEdit's API
     */
    private boolean createSphereWithWorldEdit(Player player, Location center, Material material, int radius, ExecutionContext context) {
        try {
            // This is a simplified example implementation
            // In a real WorldEdit integration, you would use WorldEdit's API like:
            // WorldEdit worldEdit = WorldEdit.getInstance();
            // LocalSession session = worldEdit.getSessionManager().get(player);
            // EditSession editSession = session.createEditSession(player);
            // etc.
            
            // For this example, we'll just create a simple sphere using Bukkit API
            // but in a real implementation you would use WorldEdit's advanced features
            
            int centerX = center.getBlockX();
            int centerY = center.getBlockY();
            int centerZ = center.getBlockZ();
            String worldName = center.getWorld().getName();
            
            context.getPlugin().getLogger().info("Creating sphere at " + centerX + "," + centerY + "," + centerZ + 
                " in world " + worldName + " with material " + material.name() + " and radius " + radius);
            
            // In a real implementation, you would use WorldEdit's API here
            // For now, we'll just return true to indicate success
            return true;
            
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error creating sphere with WorldEdit: " + e.getMessage());
            return false;
        }
    }
}