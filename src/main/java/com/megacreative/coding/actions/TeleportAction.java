package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
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
                        return getLocationFromItem(locationItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting location from container in TeleportAction: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts location from an item
     * In a real implementation, this would parse location data from the item
     * For now, we'll return the player's current location as a placeholder
     */
    private Location getLocationFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // In a real implementation, this would parse location coordinates from the display name
                // For now, we'll return null to indicate that location parsing is not implemented
                return null;
            }
        }
        return null;
    }
}