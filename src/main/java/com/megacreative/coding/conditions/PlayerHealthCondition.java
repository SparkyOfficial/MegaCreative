package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition for checking a player's health.
 * This condition returns true if the player's health matches the specified criteria.
 */
@BlockMeta(id = "playerHealth", displayName = "§aPlayer Health", type = BlockType.CONDITION)
public class PlayerHealthCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue healthValue = block.getParameter("health");
            DataValue operatorValue = block.getParameter("operator");
            
            if (healthValue == null || healthValue.isEmpty()) {
                context.getPlugin().getLogger().warning("PlayerHealthCondition: 'health' parameter is missing.");
                return false;
            }
            
            if (operatorValue == null || operatorValue.isEmpty()) {
                context.getPlugin().getLogger().warning("PlayerHealthCondition: 'operator' parameter is missing.");
                return false;
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedHealth = resolver.resolve(context, healthValue);
            DataValue resolvedOperator = resolver.resolve(context, operatorValue);
            
            double health = resolvedHealth.asNumber().doubleValue();
            String operator = resolvedOperator.asString();
            
            double playerHealth = player.getHealth();
            
            switch (operator) {
                case "equal":
                case "==":
                    return playerHealth == health;
                case "greater":
                case ">":
                    return playerHealth > health;
                case "less":
                case "<":
                    return playerHealth < health;
                case "greater_or_equal":
                case ">=":
                    return playerHealth >= health;
                case "less_or_equal":
                case "<=":
                    return playerHealth <= health;
                default:
                    context.getPlugin().getLogger().warning("PlayerHealthCondition: Invalid operator '" + operator + "'.");
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error in PlayerHealthCondition: " + e.getMessage());
            return false;
        }
    }
}