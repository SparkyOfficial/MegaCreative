package com.megacreative.coding.actions;

import com.megacreative.MegaCreative;
import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.conditions.CompareVariableCondition;
import com.megacreative.coding.conditions.IfVarEqualsCondition;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.placeholders.ReferenceSystemPlaceholderResolver;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class WhileAction implements BlockAction {
    
    // Constants for magic numbers
    private static final int DEFAULT_MAX_ITERATIONS = 1000;
    private static final int MIN_MAX_ITERATIONS = 1;
    private static final String WHILE_LOOP_CONTEXT = "while_loop";
    
    private final MegaCreative plugin;
    
    public WhileAction(MegaCreative plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player != null) {
            player.sendMessage("§eExecuting while loop action");
        }
        
        // Get the condition from the container configuration
        String condition = getConditionFromContainer(block, context);
        
        if (condition == null || condition.isEmpty()) {
            if (player != null) {
                player.sendMessage("§cWhile loop condition not configured");
            }
            return ExecutionResult.error("While loop condition not configured");
        }
        
        // Evaluate the condition
        boolean conditionResult = evaluateCondition(condition, context);
        
        if (player != null) {
            player.sendMessage("§aWhile loop condition evaluated to: " + conditionResult);
        }
        
        return ExecutionResult.success("While loop executed with condition result: " + conditionResult);
    }
    
    /**
     * Gets condition from the container configuration
     */
    private String getConditionFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get condition from the condition slot
                Integer conditionSlot = slotResolver.apply("condition");
                if (conditionSlot != null) {
                    ItemStack conditionItem = block.getConfigItem(conditionSlot);
                    if (conditionItem != null && conditionItem.hasItemMeta()) {
                        // Extract condition from item
                        return getConditionFromItem(conditionItem);
                    }
                }
            }
            
            // Fallback to parameter-based configuration
            DataValue conditionParam = block.getParameter("condition");
            if (conditionParam != null && !conditionParam.isEmpty()) {
                return conditionParam.asString();
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting condition from container in WhileAction: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts condition from an item
     */
    private String getConditionFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the condition
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Evaluates a condition string
     * Supports expressions like:
     * - "variable > 5"
     * - "counter < max"
     * - "flag == true"
     * - "apple[counter]~ < 10"
     */
    private boolean evaluateCondition(String condition, ExecutionContext context) {
        if (condition == null || condition.isEmpty()) {
            return false;
        }
        
        // Resolve any placeholders in the condition
        ParameterResolver resolver = new ParameterResolver(context);
        String resolvedCondition = resolver.resolveString(context, condition);
        
        // Handle simple boolean values
        if ("true".equalsIgnoreCase(resolvedCondition.trim())) {
            return true;
        }
        if ("false".equalsIgnoreCase(resolvedCondition.trim())) {
            return false;
        }
        
        // Handle complex expressions with operators
        // Supported operators: ==, !=, <, >, <=, >=
        String[] operators = {"==", "!=", "<=", ">=", "<", ">"};
        
        for (String operator : operators) {
            int operatorIndex = resolvedCondition.indexOf(operator);
            if (operatorIndex != -1) {
                String leftSide = resolvedCondition.substring(0, operatorIndex).trim();
                String rightSide = resolvedCondition.substring(operatorIndex + operator.length()).trim();
                
                // Try to evaluate as a numeric comparison first
                try {
                    double leftNum = Double.parseDouble(leftSide);
                    double rightNum = Double.parseDouble(rightSide);
                    
                    switch (operator) {
                        case "==": return leftNum == rightNum;
                        case "!=": return leftNum != rightNum;
                        case "<": return leftNum < rightNum;
                        case ">": return leftNum > rightNum;
                        case "<=": return leftNum <= rightNum;
                        case ">=": return leftNum >= rightNum;
                    }
                } catch (NumberFormatException e) {
                    // Not numeric, continue to string comparison
                }
                
                // String comparison
                switch (operator) {
                    case "==": return leftSide.equals(rightSide);
                    case "!=": return !leftSide.equals(rightSide);
                    case "<": return leftSide.compareTo(rightSide) < 0;
                    case ">": return leftSide.compareTo(rightSide) > 0;
                    case "<=": return leftSide.compareTo(rightSide) <= 0;
                    case ">=": return leftSide.compareTo(rightSide) >= 0;
                }
            }
        }
        
        // If we can't parse it as an expression, treat it as a variable name
        // and check if it's truthy (non-empty and not "false")
        VariableManager variableManager = context.getPlugin().getServiceRegistry().getVariableManager();
        if (variableManager != null && context.getPlayer() != null) {
            DataValue variableValue = variableManager.resolveVariable(resolvedCondition, context.getPlayer().getUniqueId().toString());
            if (variableValue != null) {
                String valueStr = variableValue.asString();
                return valueStr != null && !valueStr.isEmpty() && !"false".equalsIgnoreCase(valueStr);
            }
        }
        
        // Default fallback - if we can't evaluate it, return false for safety
        return false;
    }
}