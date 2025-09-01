package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockConfig;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;

/**
 * A generic block condition that can be dynamically created from configuration.
 * This allows for runtime-defined conditions without requiring code changes.
 */
public class GenericBlockCondition implements BlockCondition {
    private final BlockConfig config;
    
    public GenericBlockCondition(BlockConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        // Default implementation - can be overridden by subclasses
        // or configured via the BlockConfig
        
        // Log the evaluation for debugging
        context.getLogger().info("Evaluating generic condition: " + config.getName());
        
        // Get the condition type from the configuration
        String conditionType = config.getActionName();
        
        // Process based on condition type
        switch (conditionType.toUpperCase()) {
            case "HAS_ITEM":
                return evaluateHasItem(block, context);
            case "HAS_PERMISSION":
                return evaluateHasPermission(block, context);
            case "VARIABLE_COMPARE":
                return evaluateVariableCompare(block, context);
            // Add more condition types as needed
            default:
                context.getLogger().warning("Unknown condition type: " + conditionType);
                return false;
        }
    }
    
    private boolean evaluateHasItem(CodeBlock block, ExecutionContext context) {
        // Example: Check if player has a specific item
        if (context.getPlayer() == null) {
            return false;
        }
        
        String materialName = config.getParameters().getString("material", "");
        int amount = config.getParameters().getInt("amount", 1);
        
        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
            return context.getPlayer().getInventory().contains(material, amount);
        } catch (IllegalArgumentException e) {
            context.getLogger().warning("Invalid material in condition: " + materialName);
            return false;
        }
    }
    
    private boolean evaluateHasPermission(CodeBlock block, ExecutionContext context) {
        // Example: Check if player has a specific permission
        if (context.getPlayer() == null) {
            return false;
        }
        
        String permission = config.getParameters().getString("permission", "");
        if (permission.isEmpty()) {
            return false;
        }
        
        return context.getPlayer().hasPermission(permission);
    }
    
    private boolean evaluateVariableCompare(CodeBlock block, ExecutionContext context) {
        // Example: Compare a variable with a value
        String varName = config.getParameters().getString("variable", "");
        String operator = config.getParameters().getString("operator", "==");
        String value = config.getParameters().getString("value", "");
        
        if (varName.isEmpty()) {
            return false;
        }
        
        // Get the variable value from the context
        Object varValue = context.getVariableManager().getVariable(varName);
        if (varValue == null) {
            return false;
        }
        
        // Compare based on operator
        switch (operator) {
            case "==":
                return String.valueOf(varValue).equals(value);
            case "!=":
                return !String.valueOf(varValue).equals(value);
            case "<":
                try {
                    double varNum = Double.parseDouble(String.valueOf(varValue));
                    double compareNum = Double.parseDouble(value);
                    return varNum < compareNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            case ">":
                try {
                    double varNum = Double.parseDouble(String.valueOf(varValue));
                    double compareNum = Double.parseDouble(value);
                    return varNum > compareNum;
                } catch (NumberFormatException e) {
                    return false;
                }
            default:
                return false;
        }
    }
    
    @Override
    public String toString() {
        return "GenericBlockCondition{" +
                "name='" + config.getName() + '\'' +
                ", type='" + config.getActionName() + '\'' +
                '}';
    }
}
