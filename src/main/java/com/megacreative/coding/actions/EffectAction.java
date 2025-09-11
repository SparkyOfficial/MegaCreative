package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Effect;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for playing a visual effect at a location.
 * This action retrieves effect parameters from the container configuration and plays the effect.
 */
public class EffectAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get effect parameters from the container configuration
            EffectParams params = getEffectParamsFromContainer(block, context);
            
            if (params.effectNameStr == null || params.effectNameStr.isEmpty()) {
                return ExecutionResult.error("Effect is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue effectNameVal = DataValue.of(params.effectNameStr);
            DataValue resolvedEffectName = resolver.resolve(context, effectNameVal);
            
            // Parse parameters
            String effectName = resolvedEffectName.asString();
            int data = params.data;

            // Parse the effect
            Effect effect;
            try {
                effect = Effect.valueOf(effectName.toUpperCase());
            } catch (IllegalArgumentException e) {
                // Use default effect if parsing fails
                effect = Effect.ENDER_SIGNAL;
            }

            // Get the location where the effect should be played
            Location location = player.getLocation();

            // Play the effect
            player.getWorld().playEffect(location, effect, data);
            return ExecutionResult.success("Effect played successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play effect: " + e.getMessage());
        }
    }
    
    /**
     * Gets effect parameters from the container configuration
     */
    private EffectParams getEffectParamsFromContainer(CodeBlock block, ExecutionContext context) {
        EffectParams params = new EffectParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get effect name from the effect slot
                Integer effectSlot = slotResolver.apply("effect_slot");
                if (effectSlot != null) {
                    ItemStack effectItem = block.getConfigItem(effectSlot);
                    if (effectItem != null && effectItem.hasItemMeta()) {
                        // Extract effect name from item
                        params.effectNameStr = getEffectNameFromItem(effectItem);
                    }
                }
                
                // Get data from the data slot
                Integer dataSlot = slotResolver.apply("data_slot");
                if (dataSlot != null) {
                    ItemStack dataItem = block.getConfigItem(dataSlot);
                    if (dataItem != null && dataItem.hasItemMeta()) {
                        // Extract data from item
                        params.data = getDataFromItem(dataItem, 0);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting effect parameters from container in EffectAction: " + e.getMessage());
        }
        
        // Set defaults if not configured
        if (params.effectNameStr == null || params.effectNameStr.isEmpty()) {
            params.effectNameStr = "ENDER_SIGNAL";
        }
        
        return params;
    }
    
    /**
     * Extracts effect name from an item
     */
    private String getEffectNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the effect name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts data from an item
     */
    private int getDataFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse data from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper class to hold effect parameters
     */
    private static class EffectParams {
        String effectNameStr = "";
        int data = 0;
    }
}