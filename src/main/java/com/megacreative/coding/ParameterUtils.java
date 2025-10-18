package com.megacreative.coding;

import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;
import java.util.logging.Logger;

/**
 * Utility class for parameter extraction and resolution.
 * Provides common functionality for extracting parameters from both
 * the new parameter system and the old container-based system.
 */
public class ParameterUtils {
    
    /**
     * Gets a string parameter from a CodeBlock using the new parameter system
     * with a fallback to the old container-based system.
     *
     * @param block The code block to extract parameters from
     * @param context The execution context
     * @param paramName The name of the parameter to extract
     * @param slotName The name of the slot in the container (for fallback)
     * @param defaultValue The default value if parameter is not found
     * @return The extracted parameter value
     */
    public static String getStringParameter(CodeBlock block, ExecutionContext context, 
                                          String paramName, String slotName, String defaultValue) {
        try {
            
            DataValue paramValue = block.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                return paramValue.asString();
            }
            
            
            if (slotName != null && !slotName.isEmpty()) {
                String containerValue = getStringFromContainer(block, context, slotName);
                if (containerValue != null && !containerValue.isEmpty()) {
                    return containerValue;
                }
            }
        } catch (Exception e) {
            if (context != null && context.getPlugin() != null) {
                context.getPlugin().getLogger().warning("Error getting parameter '" + paramName + "': " + e.getMessage());
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Gets an integer parameter from a CodeBlock using the new parameter system
     * with a fallback to the old container-based system.
     *
     * @param block The code block to extract parameters from
     * @param context The execution context
     * @param paramName The name of the parameter to extract
     * @param slotName The name of the slot in the container (for fallback)
     * @param defaultValue The default value if parameter is not found
     * @return The extracted parameter value
     */
    public static int getIntParameter(CodeBlock block, ExecutionContext context, 
                                    String paramName, String slotName, int defaultValue) {
        try {
            
            DataValue paramValue = block.getParameter(paramName);
            if (paramValue != null && !paramValue.isEmpty()) {
                return paramValue.asNumber().intValue();
            }
            
            
            if (slotName != null && !slotName.isEmpty()) {
                String containerValue = getStringFromContainer(block, context, slotName);
                if (containerValue != null && !containerValue.isEmpty()) {
                    try {
                        
                        return Integer.parseInt(containerValue.replaceAll("[^0-9]", ""));
                    } catch (NumberFormatException e) {
                        // Log exception and continue processing
                        // This is expected behavior when parsing user input
                        Logger.getLogger(ParameterUtils.class.getName()).warning("Invalid number format: " + containerValue);
                    }
                }
            }
        } catch (Exception e) {
            if (context != null && context.getPlugin() != null) {
                context.getPlugin().getLogger().warning("Error getting parameter '" + paramName + "': " + e.getMessage());
            }
        }
        
        return defaultValue;
    }
    
    /**
     * Extracts a string value from an item in the container
     *
     * @param block The code block
     * @param context The execution context
     * @param slotName The name of the slot to extract from
     * @return The extracted string value or null if not found
     */
    private static String getStringFromContainer(CodeBlock block, ExecutionContext context, String slotName) {
        try {
            if (context == null || context.getPlugin() == null) {
                return null;
            }
            
            
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            if (blockConfigService == null) {
                return null;
            }
            
            
            String actionId = block.getAction();
            String conditionId = block.getCondition();
            String id = (actionId != null) ? actionId : conditionId;
            
            if (id == null) {
                return null;
            }
            
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(id);
            if (slotResolver == null) {
                return null;
            }
            
            
            Integer slotIndex = slotResolver.apply(slotName);
            if (slotIndex == null || slotIndex == -1) {
                return null;
            }
            
            ItemStack item = block.getConfigItem(slotIndex);
            if (item == null || !item.hasItemMeta()) {
                return null;
            }
            
            
            return getStringFromItem(item);
        } catch (Exception e) {
            if (context != null && context.getPlugin() != null) {
                context.getPlugin().getLogger().warning("Error getting string from container slot '" + slotName + "': " + e.getMessage());
            }
            return null;
        }
    }
    
    /**
     * Extracts a string value from an item
     * This method preserves color codes and other formatting
     *
     * @param item The item to extract value from
     * @return The extracted string value or null if not found
     */
    private static String getStringFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                
                return displayName;
            }
        }
        return null;
    }
    
    /**
     * Extracts a string value from an item, removing color codes
     * Use this method when you need clean text without formatting
     *
     * @param item The item to extract value from
     * @return The extracted string value with color codes removed, or null if not found
     */
    public static String getCleanStringFromItem(ItemStack item) {
        String value = getStringFromItem(item);
        if (value != null && !value.isEmpty()) {
            
            return value.replaceAll("[§][0-9a-fk-or]", "");
        }
        return value;
    }
}