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
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for getting a saved location.
 * This action retrieves a saved location and stores it in a variable from container configuration.
 */
public class GetLocationAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            GetLocationParams params = getLocationParamsFromContainer(block, context);
            
            if (params.locationName == null || params.locationName.isEmpty()) {
                return ExecutionResult.error("Location name is not configured");
            }
            
            if (params.targetName == null || params.targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedLocationName = resolver.resolveString(context, params.locationName);
            String resolvedTargetName = resolver.resolveString(context, params.targetName);

            // Get the location from the variable manager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the location from different scopes
                DataValue locationValue = null;
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    locationValue = variableManager.getVariable(resolvedLocationName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                }
                
                // Try local scope if we have a script context
                if (locationValue == null && context.getScriptId() != null) {
                    locationValue = variableManager.getVariable(resolvedLocationName, VariableScope.LOCAL, context.getScriptId());
                }
                
                // Try global scope
                if (locationValue == null) {
                    locationValue = variableManager.getVariable(resolvedLocationName, VariableScope.GLOBAL, "global");
                }
                
                // Try server scope
                if (locationValue == null) {
                    locationValue = variableManager.getVariable(resolvedLocationName, VariableScope.SERVER, "server");
                }
                
                if (locationValue != null) {
                    String locationString = locationValue.asString();
                    if (locationString != null && !locationString.isEmpty()) {
                        // Store the location string in the target variable
                        String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                        variableManager.setVariable(resolvedTargetName, DataValue.of(locationString), VariableScope.LOCAL, scriptId);
                        return ExecutionResult.success("Retrieved location '" + resolvedLocationName + "' and stored in '" + resolvedTargetName + "'");
                    }
                }
                
                return ExecutionResult.error("Location '" + resolvedLocationName + "' not found");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get location: " + e.getMessage());
        }
    }
    
    /**
     * Gets location parameters from the container configuration
     */
    private GetLocationParams getLocationParamsFromContainer(CodeBlock block, ExecutionContext context) {
        GetLocationParams params = new GetLocationParams();
        
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
                        params.locationName = getLocationNameFromItem(locationNameItem);
                    }
                }
                
                // Get target variable from the targetVariable slot
                Integer targetVariableSlot = slotResolver.apply("targetVariable");
                if (targetVariableSlot != null) {
                    ItemStack targetVariableItem = block.getConfigItem(targetVariableSlot);
                    if (targetVariableItem != null && targetVariableItem.hasItemMeta()) {
                        // Extract target variable name from item
                        params.targetName = getTargetNameFromItem(targetVariableItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting location parameters from container in GetLocationAction: " + e.getMessage());
        }
        
        return params;
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
    
    /**
     * Extracts target variable name from an item
     */
    private String getTargetNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target variable name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold location parameters
     */
    private static class GetLocationParams {
        String locationName = "";
        String targetName = "";
    }
}