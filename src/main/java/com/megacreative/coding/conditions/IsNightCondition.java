package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition for checking if it's night time in the player's world.
 * This condition returns true if it's night time in the player's world.
 */
public class IsNightCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null || player.getWorld() == null) {
            return false;
        }

        try {
            // Get the time parameter from the block (optional)
            DataValue timeValue = block.getParameter("time");
            
            // If a specific time is provided, use it
            if (timeValue != null) {
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedTime = resolver.resolve(context, timeValue);
                
                try {
                    long time = resolvedTime.asNumber().longValue();
                    // Night time in Minecraft is from 12542 to 23459 ticks
                    return time >= 12542 && time <= 23459;
                } catch (NumberFormatException e) {
                    // If parsing fails, check if it's a string representation
                    String timeString = resolvedTime.asString();
                    if (timeString != null) {
                        return "night".equalsIgnoreCase(timeString) || "dark".equalsIgnoreCase(timeString);
                    }
                    return false;
                }
            } else {
                // If no time is specified, check if it's currently night in the player's world
                long worldTime = player.getWorld().getTime();
                // Night time in Minecraft is from 12542 to 23459 ticks
                return worldTime >= 12542 && worldTime <= 23459;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}