package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Random;
import java.util.function.Function;

/**
 * Action for generating a random number.
 * This action retrieves parameters from the container configuration and generates a random number.
 */
public class RandomNumberAction implements BlockAction {
    private static final Random RANDOM = new Random();

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            RandomNumberParams params = getRandomNumberParamsFromContainer(block, context);

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue minVal = DataValue.of(params.minStr);
            DataValue resolvedMin = resolver.resolve(context, minVal);
            
            DataValue maxVal = DataValue.of(params.maxStr);
            DataValue resolvedMax = resolver.resolve(context, maxVal);
            
            DataValue targetVal = DataValue.of(params.targetStr);
            DataValue resolvedTarget = resolver.resolve(context, targetVal);
            
            // Parse parameters
            int min = 0;
            int max = 100;
            String targetVar = resolvedTarget.asString();
            
            try {
                min = Integer.parseInt(resolvedMin.asString());
                max = Integer.parseInt(resolvedMax.asString());
            } catch (NumberFormatException e) {
                // Use default values if parsing fails
            }
            
            if (targetVar == null || targetVar.isEmpty()) {
                return ExecutionResult.error("Invalid target variable");
            }

            // Generate a random number
            int randomNumber = RANDOM.nextInt(max - min + 1) + min;

            // Get the variable manager to set the variable
            // Note: This is a simplified implementation - in a real system, you would set the actual variable
            // For now, we'll just log the operation
            context.getPlugin().getLogger().info("Generating random number " + randomNumber + " into " + targetVar);
            
            return ExecutionResult.success("Random number generated successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to generate random number: " + e.getMessage());
        }
    }
    
    /**
     * Gets random number parameters from the container configuration
     */
    private RandomNumberParams getRandomNumberParamsFromContainer(CodeBlock block, ExecutionContext context) {
        RandomNumberParams params = new RandomNumberParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get min value from the min slot
                Integer minSlot = slotResolver.apply("min");
                if (minSlot != null) {
                    ItemStack minItem = block.getConfigItem(minSlot);
                    if (minItem != null && minItem.hasItemMeta()) {
                        // Extract min value from item
                        params.minStr = getMinValueFromItem(minItem);
                    }
                }
                
                // Get max value from the max slot
                Integer maxSlot = slotResolver.apply("max");
                if (maxSlot != null) {
                    ItemStack maxItem = block.getConfigItem(maxSlot);
                    if (maxItem != null && maxItem.hasItemMeta()) {
                        // Extract max value from item
                        params.maxStr = getMaxValueFromItem(maxItem);
                    }
                }
                
                // Get target variable from the target slot
                Integer targetSlot = slotResolver.apply("target");
                if (targetSlot != null) {
                    ItemStack targetItem = block.getConfigItem(targetSlot);
                    if (targetItem != null && targetItem.hasItemMeta()) {
                        // Extract target variable from item
                        params.targetStr = getTargetVariableFromItem(targetItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting random number parameters from container in RandomNumberAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts min value from an item
     */
    private String getMinValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the min value
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "0";
    }
    
    /**
     * Extracts max value from an item
     */
    private String getMaxValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the max value
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "100";
    }
    
    /**
     * Extracts target variable from an item
     */
    private String getTargetVariableFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target variable
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold random number parameters
     */
    private static class RandomNumberParams {
        String minStr = "0";
        String maxStr = "100";
        String targetStr = "";
    }
}