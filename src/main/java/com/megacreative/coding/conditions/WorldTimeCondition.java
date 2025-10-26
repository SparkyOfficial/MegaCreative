package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Condition for checking the world time.
 * This condition returns true if the world time matches the specified criteria.
 */
@BlockMeta(id = "worldTime", displayName = "§aWorld Time", type = BlockType.CONDITION)
public class WorldTimeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue timeValue = block.getParameter("time");
            DataValue operatorValue = block.getParameter("operator");
            
            if (timeValue == null || timeValue.isEmpty()) {
                context.getPlugin().getLogger().warning("WorldTimeCondition: 'time' parameter is missing.");
                return false;
            }
            
            if (operatorValue == null || operatorValue.isEmpty()) {
                context.getPlugin().getLogger().warning("WorldTimeCondition: 'operator' parameter is missing.");
                return false;
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTime = resolver.resolve(context, timeValue);
            DataValue resolvedOperator = resolver.resolve(context, operatorValue);
            
            long time = resolvedTime.asNumber().longValue();
            String operator = resolvedOperator.asString();
            
            World world = player.getWorld();
            long worldTime = world.getTime();
            
            switch (operator) {
                case "equal":
                case "==":
                    return worldTime == time;
                case "greater":
                case ">":
                    return worldTime > time;
                case "less":
                case "<":
                    return worldTime < time;
                case "greater_or_equal":
                case ">=":
                    return worldTime >= time;
                case "less_or_equal":
                case "<=":
                    return worldTime <= time;
                default:
                    context.getPlugin().getLogger().warning("WorldTimeCondition: Invalid operator '" + operator + "'.");
                    return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error in WorldTimeCondition: " + e.getMessage());
            return false;
        }
    }
}