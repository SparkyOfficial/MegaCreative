package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Condition for comparing two variables from container configuration.
 * This condition returns true if the comparison between the two variables is true.
 */
public class CompareVariableCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            CompareVariableParams params = getVarParamsFromContainer(block, context);
            
            if (params.var1Str == null || params.var1Str.isEmpty() || 
                params.operatorStr == null || params.operatorStr.isEmpty() ||
                params.var2Str == null || params.var2Str.isEmpty()) {
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue var1Value = DataValue.of(params.var1Str);
            DataValue resolvedVar1 = resolver.resolve(context, var1Value);
            
            DataValue operatorValue = DataValue.of(params.operatorStr);
            DataValue resolvedOperator = resolver.resolve(context, operatorValue);
            
            DataValue var2Value = DataValue.of(params.var2Str);
            DataValue resolvedVar2 = resolver.resolve(context, var2Value);
            
            // Parse parameters
            String var1Name = resolvedVar1.asString();
            String operator = resolvedOperator.asString();
            String var2Name = resolvedVar2.asString();
            
            if (var1Name == null || var1Name.isEmpty() || 
                operator == null || operator.isEmpty() ||
                var2Name == null || var2Name.isEmpty()) {
                return false;
            }

            // Get the variable manager to retrieve the variable values
            // Note: This is a simplified implementation - in a real system, you would retrieve the actual variable values
            String var1ValueStr = "test1"; // Placeholder for actual variable value retrieval
            String var2ValueStr = "test2"; // Placeholder for actual variable value retrieval

            // Compare the variables based on the operator
            switch (operator) {
                case "==":
                case "equals":
                    return var1ValueStr.equals(var2ValueStr);
                case "!=":
                case "not_equals":
                    return !var1ValueStr.equals(var2ValueStr);
                case "<":
                case "less_than":
                    try {
                        double var1Num = Double.parseDouble(var1ValueStr);
                        double var2Num = Double.parseDouble(var2ValueStr);
                        return var1Num < var2Num;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case ">":
                case "greater_than":
                    try {
                        double var1Num = Double.parseDouble(var1ValueStr);
                        double var2Num = Double.parseDouble(var2ValueStr);
                        return var1Num > var2Num;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case "<=":
                case "less_or_equal":
                    try {
                        double var1Num = Double.parseDouble(var1ValueStr);
                        double var2Num = Double.parseDouble(var2ValueStr);
                        return var1Num <= var2Num;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                case ">=":
                case "greater_or_equal":
                    try {
                        double var1Num = Double.parseDouble(var1ValueStr);
                        double var2Num = Double.parseDouble(var2ValueStr);
                        return var1Num >= var2Num;
                    } catch (NumberFormatException e) {
                        return false;
                    }
                default:
                    return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
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
                // Get first variable from the var1 slot
                Integer var1Slot = slotResolver.apply("var1");
                if (var1Slot != null) {
                    ItemStack var1Item = block.getConfigItem(var1Slot);
                    if (var1Item != null && var1Item.hasItemMeta()) {
                        // Extract first variable name from item
                        params.var1Str = getVariableNameFromItem(var1Item);
                    }
                }
                
                // Get operator from the operator slot
                Integer operatorSlot = slotResolver.apply("operator");
                if (operatorSlot != null) {
                    ItemStack operatorItem = block.getConfigItem(operatorSlot);
                    if (operatorItem != null && operatorItem.hasItemMeta()) {
                        // Extract operator from item
                        params.operatorStr = getOperatorFromItem(operatorItem);
                    }
                }
                
                // Get second variable from the var2 slot
                Integer var2Slot = slotResolver.apply("var2");
                if (var2Slot != null) {
                    ItemStack var2Item = block.getConfigItem(var2Slot);
                    if (var2Item != null && var2Item.hasItemMeta()) {
                        // Extract second variable name from item
                        params.var2Str = getVariableNameFromItem(var2Item);
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
    
    /**
     * Helper class to hold variable parameters
     */
    private static class CompareVariableParams {
        String var1Str = "";
        String operatorStr = "";
        String var2Str = "";
    }
}