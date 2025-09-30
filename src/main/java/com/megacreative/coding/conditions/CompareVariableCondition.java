package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.entity.Player;

/**
 * Condition for comparing two variables from the new parameter system.
 * This condition returns true if the comparison between the two variables is true.
 */
@BlockMeta(id = "compareVariable", displayName = "§aCompare Variables", type = BlockType.CONDITION)
public class CompareVariableCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the new parameter system
            DataValue var1Value = block.getParameter("var1");
            DataValue operatorValue = block.getParameter("operator");
            DataValue var2Value = block.getParameter("var2");
            
            if (var1Value == null || var1Value.isEmpty()) {
                context.getPlugin().getLogger().warning("CompareVariableCondition: 'var1' parameter is missing.");
                return false;
            }
            
            if (operatorValue == null || operatorValue.isEmpty()) {
                context.getPlugin().getLogger().warning("CompareVariableCondition: 'operator' parameter is missing.");
                return false;
            }
            
            if (var2Value == null || var2Value.isEmpty()) {
                context.getPlugin().getLogger().warning("CompareVariableCondition: 'var2' parameter is missing.");
                return false;
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedVar1 = resolver.resolve(context, var1Value);
            DataValue resolvedOperator = resolver.resolve(context, operatorValue);
            DataValue resolvedVar2 = resolver.resolve(context, var2Value);
            
            // Parse parameters
            String var1Name = resolvedVar1.asString();
            String operator = resolvedOperator.asString();
            String var2Name = resolvedVar2.asString();
            
            if (var1Name == null || var1Name.isEmpty() || 
                operator == null || operator.isEmpty() ||
                var2Name == null || var2Name.isEmpty()) {
                context.getPlugin().getLogger().warning("CompareVariableCondition: One or more parameters are empty.");
                return false;
            }

            // Get the actual variable values from the VariableManager
            VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
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
                    context.getPlugin().getLogger().warning("CompareVariableCondition: Invalid operator '" + operator + "'.");
                    return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            context.getPlugin().getLogger().warning("Error in CompareVariableCondition: " + e.getMessage());
            return false;
        }
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