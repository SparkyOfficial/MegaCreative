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

import java.util.Random;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for generating a random number.
 * This action generates a random number and stores it in a variable from container configuration.
 */
public class RandomNumberAction implements BlockAction {
    
    private static final Random random = new Random();

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get random number parameters from the container configuration
            RandomNumberParams params = getRandomNumberParamsFromContainer(block, context);
            
            if (params.targetName == null || params.targetName.isEmpty()) {
                return ExecutionResult.error("Target variable name is not configured");
            }

            // Resolve any placeholders in the target variable name
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTargetName = resolver.resolveString(context, params.targetName);

            // Generate random number
            double randomNumber = params.min + (params.max - params.min) * random.nextDouble();
            
            // Store the random number in the target variable
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                String scriptId = context.getScriptId() != null ? context.getScriptId() : "global";
                variableManager.setVariable(resolvedTargetName, DataValue.of(randomNumber), VariableScope.LOCAL, scriptId);
                return ExecutionResult.success("Generated random number " + randomNumber + " and stored in '" + resolvedTargetName + "'");
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
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
                // Get target from the target slot
                Integer targetSlot = slotResolver.apply("target_slot");
                if (targetSlot != null) {
                    ItemStack targetItem = block.getConfigItem(targetSlot);
                    if (targetItem != null && targetItem.hasItemMeta()) {
                        // Extract target from item
                        params.targetName = getTextFromItem(targetItem);
                    }
                }
                
                // Get min from the min slot
                Integer minSlot = slotResolver.apply("min_slot");
                if (minSlot != null) {
                    ItemStack minItem = block.getConfigItem(minSlot);
                    if (minItem != null && minItem.hasItemMeta()) {
                        // Extract min from item
                        params.min = getDoubleFromItem(minItem, 0);
                    }
                }
                
                // Get max from the max slot
                Integer maxSlot = slotResolver.apply("max_slot");
                if (maxSlot != null) {
                    ItemStack maxItem = block.getConfigItem(maxSlot);
                    if (maxItem != null && maxItem.hasItemMeta()) {
                        // Extract max from item
                        params.max = getDoubleFromItem(maxItem, 100);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting random number parameters from container in RandomNumberAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts text from an item
     */
    private String getTextFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the text
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Extracts double from an item
     */
    private double getDoubleFromItem(ItemStack item, double defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse double from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Double.parseDouble(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper class to hold random number parameters
     */
    private static class RandomNumberParams {
        String targetName = "";
        double min = 0;
        double max = 100;
    }
}