package com.megacreative.coding.conditions;

import com.megacreative.coding.config.BlockConfig;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * A generic block condition that can be dynamically created from configuration.
 * This allows for runtime-defined conditions without requiring code changes.
 */
@BlockMeta(id = "genericCondition", displayName = "§aGeneric Condition", type = BlockType.CONDITION)
public class GenericBlockCondition implements BlockCondition {
    private final BlockConfig config;
    
    public GenericBlockCondition(BlockConfig config) {
        this.config = config;
    }
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        
        
        
        
        context.getPlugin().getLogger().info("Evaluating generic condition: " + config.getName());
        
        
        String conditionType = config.getActionName();
        
        
        switch (conditionType.toUpperCase()) {
            case "HAS_ITEM":
                return evaluateHasItem(block, context);
            case "HAS_PERMISSION":
                return evaluateHasPermission(block, context);
            case "VARIABLE_COMPARE":
                return evaluateVariableCompare(block, context);
            
            default:
                context.getPlugin().getLogger().warning("Unknown condition type: " + conditionType);
                return false;
        }
    }
    
    private boolean evaluateHasItem(CodeBlock block, ExecutionContext context) {
        
        if (context.getPlayer() == null) {
            return false;
        }
        
        
        Object materialObj = block.getParameter("material");
        String materialName = materialObj != null ? materialObj.toString() : "STONE"; 
        
        Object amountObj = block.getParameter("amount");
        int amount = amountObj instanceof Number ? ((Number) amountObj).intValue() : 1;
        
        try {
            org.bukkit.Material material = org.bukkit.Material.valueOf(materialName.toUpperCase());
            return context.getPlayer().getInventory().contains(material, amount);
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid material in condition: " + materialName);
            return false;
        }
    }
    
    private boolean evaluateHasPermission(CodeBlock block, ExecutionContext context) {
        
        if (context.getPlayer() == null) {
            return false;
        }
        
        Object permissionObj = block.getParameter("permission");
        String permission = permissionObj != null ? permissionObj.toString() : null;
        
        if (permission == null || permission.isEmpty()) {
            return false;
        }
        
        return context.getPlayer().hasPermission(permission);
    }
    
    private boolean evaluateVariableCompare(CodeBlock block, ExecutionContext context) {
        
        Object varNameObj = block.getParameter("variable");
        String varName = varNameObj != null ? varNameObj.toString() : null;
        
        Object operatorObj = block.getParameter("operator");
        String operator = operatorObj != null ? operatorObj.toString() : "==";
        
        Object valueObj = block.getParameter("value");
        String value = valueObj != null ? valueObj.toString() : "";
        
        if (varName == null || varName.isEmpty()) {
            return false;
        }
        
        
        Object varValue = context.getVariable(varName);
        if (varValue == null) {
            return false;
        }
        
        
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