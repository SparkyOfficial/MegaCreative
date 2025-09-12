package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for teleporting a player to a location.
 * This action retrieves location coordinates from the container configuration and teleports the player.
 */
public class TeleportAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the location from the container configuration
            Location location = getLocationFromContainer(block, context);
            
            if (location == null) {
                return ExecutionResult.error("Location is not configured");
            }

            // Teleport the player
            player.teleport(location);
            return ExecutionResult.success("Player teleported successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to teleport player: " + e.getMessage());
        }
    }
    
    /**
     * Gets location from the container configuration
     */
    private Location getLocationFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get location from the location slot
                Integer locationSlot = slotResolver.apply("location_slot");
                if (locationSlot != null) {
                    ItemStack locationItem = block.getConfigItem(locationSlot);
                    if (locationItem != null && locationItem.hasItemMeta()) {
                        // Extract location from item
                        return getLocationFromItem(locationItem, context);
                    }
                }
            }
            
            // 🎆 ENHANCED: Fallback to parameter-based configuration
            DataValue xParam = block.getParameter("x");
            DataValue yParam = block.getParameter("y");
            DataValue zParam = block.getParameter("z");
            DataValue worldParam = block.getParameter("world");
            
            if (xParam != null && yParam != null && zParam != null) {
                double x = Double.parseDouble(xParam.asString());
                double y = Double.parseDouble(yParam.asString());
                double z = Double.parseDouble(zParam.asString());
                
                World world = context.getPlayer().getWorld(); // Default to current world
                if (worldParam != null && !worldParam.isEmpty()) {
                    World targetWorld = org.bukkit.Bukkit.getWorld(worldParam.asString());
                    if (targetWorld != null) {
                        world = targetWorld;
                    }
                }
                
                return new Location(world, x, y, z);
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting location from container in TeleportAction: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 🎆 ENHANCED: Extracts location from an item with proper parsing
     */
    private Location getLocationFromItem(ItemStack item, ExecutionContext context) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Parse location from display name format: "x,y,z" or "x,y,z,world"
                String locationStr = org.bukkit.ChatColor.stripColor(displayName).trim();
                String[] parts = locationStr.split(",");
                
                try {
                    if (parts.length >= 3) {
                        double x = Double.parseDouble(parts[0].trim());
                        double y = Double.parseDouble(parts[1].trim());
                        double z = Double.parseDouble(parts[2].trim());
                        
                        World world = context.getPlayer().getWorld(); // Default to current world
                        if (parts.length >= 4) {
                            World targetWorld = org.bukkit.Bukkit.getWorld(parts[3].trim());
                            if (targetWorld != null) {
                                world = targetWorld;
                            }
                        }
                        
                        return new Location(world, x, y, z);
                    }
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("Invalid location format in item: " + locationStr);
                }
            }
            
            // Check lore for location data
            if (meta.hasLore()) {
                for (String line : meta.getLore()) {
                    String cleanLine = org.bukkit.ChatColor.stripColor(line).trim();
                    if (cleanLine.startsWith("Location:") || cleanLine.startsWith("Loc:")) {
                        String locationPart = cleanLine.substring(cleanLine.indexOf(":") + 1).trim();
                        String[] parts = locationPart.split(",");
                        
                        try {
                            if (parts.length >= 3) {
                                double x = Double.parseDouble(parts[0].trim());
                                double y = Double.parseDouble(parts[1].trim());
                                double z = Double.parseDouble(parts[2].trim());
                                
                                World world = context.getPlayer().getWorld();
                                if (parts.length >= 4) {
                                    World targetWorld = org.bukkit.Bukkit.getWorld(parts[3].trim());
                                    if (targetWorld != null) {
                                        world = targetWorld;
                                    }
                                }
                                
                                return new Location(world, x, y, z);
                            }
                        } catch (NumberFormatException e) {
                            // Continue to next lore line
                        }
                    }
                }
            }
        }
        return null;
    }
}