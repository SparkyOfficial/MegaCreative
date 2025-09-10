package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition for checking the world time.
 * This condition returns true if the world time meets the specified criteria.
 */
public class WorldTimeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null || player.getWorld() == null) {
            return false;
        }

        try {
            // Get the time parameter from the block
            DataValue timeValue = block.getParameter("time");
            if (timeValue == null) {
                return false;
            }

            // Get optional comparison operator (default to "equal")
            String operator = "equal";
            DataValue operatorValue = block.getParameter("operator");
            if (operatorValue != null) {
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedOperator = resolver.resolve(context, operatorValue);
                String op = resolvedOperator.asString();
                if (op != null && !op.isEmpty()) {
                    operator = op.toLowerCase();
                }
            }

            // Resolve any placeholders in the time value
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTime = resolver.resolve(context, timeValue);
            
            // Parse time parameter
            long time;
            try {
                time = resolvedTime.asNumber().longValue();
            } catch (NumberFormatException e) {
                return false;
            }

            // Check world time against the specified value
            long worldTime = player.getWorld().getTime();
            
            switch (operator) {
                case "equal":
                case "equals":
                    return worldTime == time;
                case "greater":
                    return worldTime > time;
                case "greater_or_equal":
                    return worldTime >= time;
                case "less":
                    return worldTime < time;
                case "less_or_equal":
                    return worldTime <= time;
                default:
                    return worldTime == time; // Default to equal
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}