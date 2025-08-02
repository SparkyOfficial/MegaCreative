package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class PlayerHealthCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        Object rawHealth = block.getParameter("health");
        Object rawOperator = block.getParameter("operator");
        
        String healthStr = ParameterResolver.resolve(context, rawHealth);
        String operatorStr = ParameterResolver.resolve(context, rawOperator);
        
        if (healthStr == null || operatorStr == null) return false;
        
        try {
            double requiredHealth = Double.parseDouble(healthStr);
            double currentHealth = player.getHealth();
            
            switch (operatorStr) {
                case ">":
                    return currentHealth > requiredHealth;
                case ">=":
                    return currentHealth >= requiredHealth;
                case "<":
                    return currentHealth < requiredHealth;
                case "<=":
                    return currentHealth <= requiredHealth;
                case "==":
                case "=":
                    return currentHealth == requiredHealth;
                default:
                    return false;
            }
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 