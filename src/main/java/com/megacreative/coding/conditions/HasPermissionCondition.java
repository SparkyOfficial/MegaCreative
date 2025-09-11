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
 * Condition for checking if a player has a specific permission from container configuration.
 * This condition retrieves a permission string from the container configuration and checks if the player has it.
 */
public class HasPermissionCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the permission from the container configuration
            String permission = getPermissionFromContainer(block, context);
            if (permission == null || permission.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the permission
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedPermission = resolver.resolveString(context, permission);
            
            // Check if the player has the permission
            if (resolvedPermission != null && !resolvedPermission.isEmpty()) {
                return player.hasPermission(resolvedPermission);
            }
            
            return false;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets permission from the container configuration
     */
    private String getPermissionFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get permission from the permission_slot
                Integer permissionSlot = slotResolver.apply("permission_slot");
                if (permissionSlot != null) {
                    ItemStack permissionItem = block.getConfigItem(permissionSlot);
                    if (permissionItem != null && permissionItem.hasItemMeta()) {
                        // Extract permission from item
                        return getPermissionFromItem(permissionItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting permission from container in HasPermissionCondition: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts permission from an item
     */
    private String getPermissionFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the permission
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}