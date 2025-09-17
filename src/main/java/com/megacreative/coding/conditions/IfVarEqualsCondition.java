package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for checking if a variable equals a specific value from container configuration.
 * This condition returns true if the specified variable equals the specified value.
 */
public class IfVarEqualsCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            IfVarEqualsParams params = getVarParamsFromContainer(block, context);
            
            if (params.nameStr == null || params.nameStr.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue nameValue = DataValue.of(params.nameStr);
            DataValue resolvedName = resolver.resolve(context, nameValue);
            
            DataValue valueValue = DataValue.of(params.valueStr);
            DataValue resolvedValue = resolver.resolve(context, valueValue);
            
            // Parse parameters
            String varName = resolvedName.asString();
            String compareValue = resolvedValue.asString();
            
            if (varName == null || varName.isEmpty()) {
                return false;
            }

            // Get the actual variable value from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            Object varValue = null;
            
            // Try to get the variable from different scopes
            // First try player variables
            if (player != null) {
                DataValue playerVar = variableManager.getPlayerVariable(player.getUniqueId(), varName);
                if (playerVar != null) {
                    varValue = playerVar.getValue();
                }
            }
            
            // If not found, try local variables
            if (varValue == null) {
                DataValue localVar = variableManager.getLocalVariable(context.getScriptId(), varName);
                if (localVar != null) {
                    varValue = localVar.getValue();
                }
            }
            
            // If not found, try global variables
            if (varValue == null) {
                DataValue globalVar = variableManager.getGlobalVariable(varName);
                if (globalVar != null) {
                    varValue = globalVar.getValue();
                }
            }
            
            // If not found, try server variables
            if (varValue == null) {
                DataValue serverVar = variableManager.getServerVariable(varName);
                if (serverVar != null) {
                    varValue = serverVar.getValue();
                }
            }

            // Compare the variable value with the specified value
            if (varValue != null) {
                return varValue.toString().equals(compareValue);
            } else {
                // If variable doesn't exist, compare with empty string
                return "".equals(compareValue);
            }
        } catch (Exception e) {
            // If there's an error, return false
            context.getPlugin().getLogger().warning("Error in IfVarEqualsCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private IfVarEqualsParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IfVarEqualsParams params = new IfVarEqualsParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
                    ItemStack nameItem = block.getConfigItem(nameSlot);
                    if (nameItem != null && nameItem.hasItemMeta()) {
                        // Extract variable name from item
                        params.nameStr = getVariableNameFromItem(nameItem);
                    }
                }
                
                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null) {
                        // Extract value from item
                        params.valueStr = getValueFromItem(valueItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in IfVarEqualsCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts variable name from an item
     */
    private String getVariableNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the variable name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts value from an item
     */
    private String getValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the value
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        
        // If no display name, use the item type name
        return item.getType().name();
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class IfVarEqualsParams {
        String nameStr = "";
        String valueStr = "";
    }
}