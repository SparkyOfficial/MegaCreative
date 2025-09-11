package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a player is in a specific world from container configuration.
 * This condition returns true if the player is in the specified world.
 */
public class IsInWorldCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the world name from the container configuration
            String worldName = getWorldFromContainer(block, context);
            if (worldName == null || worldName.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the world name
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedWorldName = resolver.resolveString(context, worldName);
            
            // Parse world name parameter
            if (resolvedWorldName == null || resolvedWorldName.isEmpty()) {
                return false;
            }

            // Check if player is in the specified world
            return player.getWorld().getName().equals(resolvedWorldName);
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets world name from the container configuration
     */
    private String getWorldFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get world from the world_slot
                Integer worldSlot = slotResolver.apply("world_slot");
                if (worldSlot != null) {
                    ItemStack worldItem = block.getConfigItem(worldSlot);
                    if (worldItem != null && worldItem.hasItemMeta()) {
                        // Extract world name from item
                        return getWorldNameFromItem(worldItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting world name from container in IsInWorldCondition: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts world name from an item
     */
    private String getWorldNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the world name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}