package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class PlayerHealthCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawHealth = block.getParameter("health");
        DataValue rawOperator = block.getParameter("operator");

        if (rawHealth == null) {
            context.getPlugin().getLogger().warning("Health not specified in PlayerHealthCondition");
            return false;
        }

        DataValue healthValue = resolver.resolve(context, rawHealth);
        DataValue operatorValue = rawOperator != null ? resolver.resolve(context, rawOperator) : null;

        String operator = operatorValue != null ? operatorValue.asString() : ">=";
        
        try {
            double requiredHealth = Double.parseDouble(healthValue.asString());
            double playerHealth = player.getHealth();
            
            switch (operator) {
                case ">":
                    return playerHealth > requiredHealth;
                case ">=":
                    return playerHealth >= requiredHealth;
                case "<":
                    return playerHealth < requiredHealth;
                case "<=":
                    return playerHealth <= requiredHealth;
                case "==":
                    return playerHealth == requiredHealth;
                case "!=":
                    return playerHealth != requiredHealth;
                default:
                    return playerHealth >= requiredHealth; // Default to >=
            }
        } catch (NumberFormatException e) {
            context.getPlugin().getLogger().warning("Invalid health value in PlayerHealthCondition: " + healthValue.asString());
            return false;
        }
    }
}