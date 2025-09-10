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
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for multiplying a variable by a value.
 * This action multiplies an existing variable by a value from container configuration.
 */
public class MulVarAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            MulVarParams params = getVarParamsFromContainer(block, context);
            
            if (params.varName == null || params.varName.isEmpty()) {
                return ExecutionResult.error("Variable name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedVarName = resolver.resolveString(context, params.varName);
            DataValue resolvedValue = resolver.resolve(context, params.value);

            // Get the variable using the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            if (variableManager != null) {
                // Try to get the variable from different scopes
                DataValue variableValue = null;
                VariableScope variableScope = VariableScope.LOCAL;
                String variableContext = context.getScriptId() != null ? context.getScriptId() : "global";
                
                // Try player scope first if we have a player
                if (context.getPlayer() != null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
                    if (variableValue != null) {
                        variableScope = VariableScope.PLAYER;
                        variableContext = context.getPlayer().getUniqueId().toString();
                    }
                }
                
                // Try local scope if we have a script context
                if (variableValue == null && context.getScriptId() != null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.LOCAL, context.getScriptId());
                    if (variableValue != null) {
                        variableScope = VariableScope.LOCAL;
                        variableContext = context.getScriptId();
                    }
                }
                
                // Try global scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.GLOBAL, "global");
                    if (variableValue != null) {
                        variableScope = VariableScope.GLOBAL;
                        variableContext = "global";
                    }
                }
                
                // Try server scope
                if (variableValue == null) {
                    variableValue = variableManager.getVariable(resolvedVarName, VariableScope.SERVER, "server");
                    if (variableValue != null) {
                        variableScope = VariableScope.SERVER;
                        variableContext = "server";
                    }
                }
                
                if (variableValue != null) {
                    try {
                        // Try to multiply as numbers
                        double varNum = Double.parseDouble(variableValue.asString());
                        double mulNum = Double.parseDouble(resolvedValue.asString());
                        double result = varNum * mulNum;
                        
                        // Set the new value
                        variableManager.setVariable(resolvedVarName, DataValue.of(result), variableScope, variableContext);
                        return ExecutionResult.success("Multiplied variable '" + resolvedVarName + "' by " + mulNum + ", result: " + result);
                    } catch (NumberFormatException e) {
                        return ExecutionResult.error("Cannot multiply non-numeric variable '" + resolvedVarName + "'");
                    }
                } else {
                    // Variable doesn't exist, create it with the value
                    variableManager.setVariable(resolvedVarName, resolvedValue, VariableScope.LOCAL, 
                        context.getScriptId() != null ? context.getScriptId() : "global");
                    return ExecutionResult.success("Created variable '" + resolvedVarName + "' with value: " + resolvedValue.asString());
                }
            } else {
                return ExecutionResult.error("Variable manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to multiply variable: " + e.getMessage());
        }
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private MulVarParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        MulVarParams params = new MulVarParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get variable name from the name slot
                Integer nameSlot = slotResolver.apply("name");
                if (nameSlot != null) {
                    ItemStack nameItem = block.getConfigItem(nameSlot);
                    if (nameItem != null && nameItem.hasItemMeta()) {
                        // Extract variable name from item
                        params.varName = getVarNameFromItem(nameItem);
                    }
                }
                
                // Get value from the value slot
                Integer valueSlot = slotResolver.apply("value");
                if (valueSlot != null) {
                    ItemStack valueItem = block.getConfigItem(valueSlot);
                    if (valueItem != null) {
                        // Extract value from item
                        params.value = getValueFromItem(valueItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in MulVarAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts variable name from an item
     */
    private String getVarNameFromItem(ItemStack item) {
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
     * In a real implementation, this would parse the value based on the item type
     * For now, we'll create a simple string value
     */
    private DataValue getValueFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the value
                String cleanValue = displayName.replaceAll("[§0-9]", "").trim();
                return DataValue.of(cleanValue);
            }
        }
        
        // Fallback to item type
        return DataValue.of(item.getType().name());
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class MulVarParams {
        String varName = "";
        DataValue value = DataValue.of("");
    }
}