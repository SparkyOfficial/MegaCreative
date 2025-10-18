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
 * Condition for checking world time from the new parameter system.
 * This condition returns true if the world time matches the specified criteria.
 */
@BlockMeta(id = "worldTime", displayName = "§aWorld Time", type = BlockType.CONDITION)
public class WorldTimeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        // Condition player.getWorld() == null is always false when reached
        // Removed redundant null check since we already check for null above
        if (player == null) {
            return false;
        }

        try {
            
            DataValue timeValue = block.getParameter("time");
            DataValue operatorValue = block.getParameter("operator");
            
            
            long worldTime = 0;
            if (timeValue != null && !timeValue.isEmpty()) {
                try {
                    worldTime = timeValue.asNumber().longValue();
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("WorldTimeCondition: Invalid time value, using default 0.");
                    worldTime = 0;
                }
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTime = resolver.resolve(context, DataValue.of(String.valueOf(worldTime)));
            
            long time;
            try {
                time = Long.parseLong(resolvedTime.asString());
            } catch (NumberFormatException e) {
                context.getPlugin().getLogger().warning("WorldTimeCondition: Invalid resolved time value, using default 0.");
                time = 0;
            }

            
            String operator = "equal";
            if (operatorValue != null && !operatorValue.isEmpty()) {
                DataValue resolvedOperator = resolver.resolve(context, operatorValue);
                operator = resolvedOperator.asString();
            }
            operator = operator != null && !operator.isEmpty() ? operator : "equal";

            
            long currentWorldTime = player.getWorld().getTime();

            
            switch (operator.toLowerCase()) {
                case "equal":
                case "equals":
                case "==":
                    return currentWorldTime == time;
                case "greater":
                case "greater_than":
                case ">":
                    return currentWorldTime > time;
                case "less":
                case "less_than":
                case "<":
                    return currentWorldTime < time;
                case "greater_or_equal":
                case ">=":
                    return currentWorldTime >= time;
                case "less_or_equal":
                case "<=":
                    return currentWorldTime <= time;
                default:
                    context.getPlugin().getLogger().warning("WorldTimeCondition: Unknown operator '" + operator + "', using 'equal'.");
                    return currentWorldTime == time;
            }
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in WorldTimeCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold time parameters
     */
    private static class WorldTimeParams {
        String timeStr = "";
        String operatorStr = "";
    }
}