package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition for checking a player's health.
 * This condition returns true if the player's health meets the specified criteria.
 */
public class PlayerHealthCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the health parameter from the block
            DataValue healthValue = block.getParameter("health");
            if (healthValue == null) {
                return false;
            }

            // Resolve any placeholders in the health value
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedHealth = resolver.resolve(context, healthValue);
            
            // Parse health parameter
            double health;
            try {
                health = resolvedHealth.asNumber().doubleValue();
            } catch (NumberFormatException e) {
                return false;
            }

            // Get optional comparison operator (default to "greater_or_equal")
            String operator = "greater_or_equal";
            DataValue operatorValue = block.getParameter("operator");
            if (operatorValue != null) {
                DataValue resolvedOperator = resolver.resolve(context, operatorValue);
                String op = resolvedOperator.asString();
                if (op != null && !op.isEmpty()) {
                    operator = op.toLowerCase();
                }
            }

            // Check player's health against the specified value
            double playerHealth = player.getHealth();
            
            switch (operator) {
                case "equal":
                case "equals":
                    return playerHealth == health;
                case "greater":
                    return playerHealth > health;
                case "greater_or_equal":
                    return playerHealth >= health;
                case "less":
                    return playerHealth < health;
                case "less_or_equal":
                    return playerHealth <= health;
                default:
                    return playerHealth >= health; // Default to greater_or_equal
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}