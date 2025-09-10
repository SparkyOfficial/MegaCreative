package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.variables.IVariableManager.VariableScope;
import com.megacreative.services.BlockConfigService;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for comparing values of variables from container configuration.
 */
public class CompareVariableCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        try {
            // Get parameters from the container configuration
            CompareVariableParams params = getVarParamsFromContainer(block, context);
            
            if (params.var1Name == null || params.var1Name.isEmpty() || 
                params.var2Name == null || params.var2Name.isEmpty()) {
                context.getPlugin().getLogger().warning("Variables not specified in CompareVariableCondition");
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedVar1Name = resolver.resolveString(context, params.var1Name);
            String resolvedVar2Name = resolver.resolveString(context, params.var2Name);
            String resolvedOperator = resolver.resolveString(context, params.operator);

            String operator = resolvedOperator != null && !resolvedOperator.isEmpty() ? resolvedOperator : "==";
            
            try {
                // Get the actual variable values from VariableManager
                VariableManager variableManager = context.getPlugin().getVariableManager();
                if (variableManager != null) {
                    // Get first variable value
                    DataValue var1Value = getVariableValue(variableManager, resolvedVar1Name, context);
                    if (var1Value == null) {
                        context.getPlugin().getLogger().warning("Variable '" + resolvedVar1Name + "' not found in CompareVariableCondition");
                        return false;
                    }
                    
                    // Get second variable value
                    DataValue var2Value = getVariableValue(variableManager, resolvedVar2Name, context);
                    if (var2Value == null) {
                        context.getPlugin().getLogger().warning("Variable '" + resolvedVar2Name + "' not found in CompareVariableCondition");
                        return false;
                    }
                    
                    // Convert both values to strings for comparison
                    String value1 = var1Value.asString();
                    String value2 = var2Value.asString();
                    
                    // Try to compare as numbers if possible
                    try {
                        double num1 = Double.parseDouble(value1);
                        double num2 = Double.parseDouble(value2);
                        
                        switch (operator) {
                            case ">":
                                return num1 > num2;
                            case ">=":
                                return num1 >= num2;
                            case "<":
                                return num1 < num2;
                            case "<=":
                                return num1 <= num2;
                            case "!=":
                                return num1 != num2;
                            case "==":
                            default:
                                return num1 == num2;
                        }
                    } catch (NumberFormatException e) {
                        // If not numbers, compare as strings
                        switch (operator) {
                            case "!=":
                                return !value1.equals(value2);
                            case "==":
                            default:
                                return value1.equals(value2);
                        }
                    }
                }
            } catch (Exception e) {
                context.getPlugin().getLogger().warning("Error in CompareVariableCondition: " + e.getMessage());
                return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting parameters in CompareVariableCondition: " + e.getMessage());
        }
        
        return false;
    }
    
    /**
     * Gets variable parameters from the container configuration
     */
    private CompareVariableParams getVarParamsFromContainer(CodeBlock block, ExecutionContext context) {
        CompareVariableParams params = new CompareVariableParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get first variable name from the var1 slot
                Integer var1Slot = slotResolver.apply("var1");
                if (var1Slot != null) {
                    ItemStack var1Item = block.getConfigItem(var1Slot);
                    if (var1Item != null && var1Item.hasItemMeta()) {
                        // Extract first variable name from item
                        params.var1Name = getVarNameFromItem(var1Item);
                    }
                }
                
                // Get operator from the operator slot
                Integer operatorSlot = slotResolver.apply("operator");
                if (operatorSlot != null) {
                    ItemStack operatorItem = block.getConfigItem(operatorSlot);
                    if (operatorItem != null && operatorItem.hasItemMeta()) {
                        // Extract operator from item
                        params.operator = getOperatorFromItem(operatorItem);
                    }
                }
                
                // Get second variable name from the var2 slot
                Integer var2Slot = slotResolver.apply("var2");
                if (var2Slot != null) {
                    ItemStack var2Item = block.getConfigItem(var2Slot);
                    if (var2Item != null && var2Item.hasItemMeta()) {
                        // Extract second variable name from item
                        params.var2Name = getVarNameFromItem(var2Item);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting variable parameters from container in CompareVariableCondition: " + e.getMessage());
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
     * Extracts operator from an item
     */
    private String getOperatorFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the operator
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    private DataValue getVariableValue(VariableManager variableManager, String varName, ExecutionContext context) {
        // Try to get the variable from different scopes
        DataValue varValue = null;
        
        // Try player scope first if we have a player
        if (context.getPlayer() != null) {
            varValue = variableManager.getVariable(varName, VariableScope.PLAYER, context.getPlayer().getUniqueId().toString());
        }
        
        // Try local scope if we have a script context
        if (varValue == null && context.getScriptId() != null) {
            varValue = variableManager.getVariable(varName, VariableScope.LOCAL, context.getScriptId());
        }
        
        // Try global scope
        if (varValue == null) {
            varValue = variableManager.getVariable(varName, VariableScope.GLOBAL, "global");
        }
        
        // Try server scope
        if (varValue == null) {
            varValue = variableManager.getVariable(varName, VariableScope.SERVER, "server");
        }
        
        return varValue;
    }
    
    /**
     * Helper class to hold variable parameters
     */
    private static class CompareVariableParams {
        String var1Name = "";
        String operator = "";
        String var2Name = "";
    }
}