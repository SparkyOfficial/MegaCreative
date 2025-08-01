package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.Player;

public class PlayerHealthCondition implements BlockCondition {
    @Override
    public boolean check(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        Object rawHealth = block.getParameter("health");
        Object rawOperator = block.getParameter("operator");
        
        String healthStr = ParameterResolver.resolve(context, rawHealth).toString();
        String operator = ParameterResolver.resolve(context, rawOperator).toString();
        
        try {
            double requiredHealth = Double.parseDouble(healthStr);
            double currentHealth = player.getHealth();
            
            return switch (operator) {
                case ">" -> currentHealth > requiredHealth;
                case "<" -> currentHealth < requiredHealth;
                case ">=" -> currentHealth >= requiredHealth;
                case "<=" -> currentHealth <= requiredHealth;
                case "==" -> currentHealth == requiredHealth;
                case "!=" -> currentHealth != requiredHealth;
                default -> false;
            };
        } catch (NumberFormatException e) {
            return false;
        }
    }
} 