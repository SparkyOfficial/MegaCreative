package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for saving a player's current location.
 * This action saves the player's current location to a variable from container configuration.
 */
public class SaveLocationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the location name from the container configuration
            String locationName = getLocationNameFromContainer(block, context);
            if (locationName == null || locationName.isEmpty()) {
                return ExecutionResult.error("Location name is not configured");
            }

            // Resolve any placeholders in the location name
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedLocationName = resolver.resolveString(context, locationName);

            // Get the player's current location
            Location location = player.getLocation();
            
            // Create a string representation of the location
            String locationString = String.format("%s:%.2f:%.2f:%.2f:%.2f:%.2f", 
                location.getWorld().getName(),
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getYaw(),
                location.getPitch());
            
            // Save the location to a variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                variableManager.setVariable(resolvedLocationName, DataValue.of(locationString), VariableScope.LOCAL, scriptId);
                return ExecutionResult.success("Saved location '" + resolvedLocationName + "'");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to save location: " + e.getMessage());
        }
    }
    
    /**
     * Gets location name from the container configuration
     */
    private String getLocationNameFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get location name from the locationName slot
                Integer locationNameSlot = slotResolver.apply("locationName");
                if (locationNameSlot != null) {
                    ItemStack locationNameItem = block.getConfigItem(locationNameSlot);
                    if (locationNameItem != null && locationNameItem.hasItemMeta()) {
                        // Extract location name from item
                        return getLocationNameFromItem(locationNameItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting location name from container in SaveLocationAction: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts location name from an item
     */
    private String getLocationNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the location name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}