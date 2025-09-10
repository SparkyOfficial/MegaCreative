package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Condition for checking if there's a mob near the player.
 * This condition returns true if there's a mob within a specified distance of the player.
 */
public class MobNearCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get optional distance parameter (default to 10)
            int distance = 10;
            ParameterResolver resolver = new ParameterResolver(context);
            
            DataValue distanceValue = block.getParameter("distance");
            if (distanceValue != null) {
                DataValue resolvedDistance = resolver.resolve(context, distanceValue);
                try {
                    distance = Math.max(1, resolvedDistance.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default distance if parsing fails
                }
            }

            // Get optional mob type parameter
            String mobType = null;
            DataValue mobValue = block.getParameter("mob");
            if (mobValue != null) {
                DataValue resolvedMob = resolver.resolve(context, mobValue);
                mobType = resolvedMob.asString();
            }

            // Check for mobs near the player
            for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
                if (entity instanceof LivingEntity) {
                    // If a specific mob type is specified, check if it matches
                    if (mobType != null && !mobType.isEmpty()) {
                        if (entity.getType().name().equalsIgnoreCase(mobType)) {
                            return true;
                        }
                    } else {
                        // Any mob is acceptable
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}