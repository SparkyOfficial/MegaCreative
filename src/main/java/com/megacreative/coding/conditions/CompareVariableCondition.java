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

            // Get the actual variable values from the VariableManager
            VariableManager variableManager = context.getPlugin().getVariableManager();
            Object var1ValueObj = null;
            Object var2ValueObj = null;
            
            // Try to get the first variable from different scopes
            // First try player variables
            if (player != null) {
                DataValue playerVar = variableManager.getPlayerVariable(player.getUniqueId(), var1Name);
                if (playerVar != null) {
                    var1ValueObj = playerVar.getValue();
                }
            }
            
            // If not found, try local variables
            if (var1ValueObj == null) {
                DataValue localVar = variableManager.getLocalVariable(context.getScriptId(), var1Name);
                if (localVar != null) {
                    var1ValueObj = localVar.getValue();
                }
            }
            
            // If not found, try global variables
            if (var1ValueObj == null) {
                DataValue globalVar = variableManager.getGlobalVariable(var1Name);
                if (globalVar != null) {
                    var1ValueObj = globalVar.getValue();
                }
            }
            
            // If not found, try server variables
            if (var1ValueObj == null) {
                DataValue serverVar = variableManager.getServerVariable(var1Name);
                if (serverVar != null) {
                    var1ValueObj = serverVar.getValue();
                }
            }
            
            // Try to get the second variable from different scopes
            // First try player variables
            if (player != null) {
                DataValue playerVar = variableManager.getPlayerVariable(player.getUniqueId(), var2Name);
                if (playerVar != null) {
                    var2ValueObj = playerVar.getValue();
                }
            }
            
            // If not found, try local variables
            if (var2ValueObj == null) {
                DataValue localVar = variableManager.getLocalVariable(context.getScriptId(), var2Name);
                if (localVar != null) {
                    var2ValueObj = localVar.getValue();
                }
            }
            
            // If not found, try global variables
            if (var2ValueObj == null) {
                DataValue globalVar = variableManager.getGlobalVariable(var2Name);
                if (globalVar != null) {
                    var2ValueObj = globalVar.getValue();
                }
            }
            
            // If not found, try server variables
            if (var2ValueObj == null) {
                DataValue serverVar = variableManager.getServerVariable(var2Name);
                if (serverVar != null) {
                    var2ValueObj = serverVar.getValue();
                }
            }

            // Convert values to strings for comparison
            String var1ValueStr = var1ValueObj != null ? var1ValueObj.toString() : "";
            String var2ValueStr = var2ValueObj != null ? var2ValueObj.toString() : "";

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
            context.getPlugin().getLogger().warning("Error in CompareVariableCondition: " + e.getMessage());
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