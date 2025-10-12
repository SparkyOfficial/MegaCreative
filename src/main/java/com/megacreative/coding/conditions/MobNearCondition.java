package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a mob is near the player from the new parameter system.
 * This condition returns true if a mob is within a specified distance of the player.
 */
@BlockMeta(id = "mobNear", displayName = "§aMob Near", type = BlockType.CONDITION)
public class MobNearCondition implements BlockCondition {

    @Override
public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue mobValue = block.getParameter("mob");
            DataValue distanceValue = block.getParameter("distance");
            
            
            int distance = 10;
            if (distanceValue != null && !distanceValue.isEmpty()) {
                try {
                    distance = Math.max(1, distanceValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("MobNearCondition: Invalid distance value, using default 10.");
                }
            }

            
            for (Entity entity : player.getNearbyEntities(distance, distance, distance)) {
                if (entity instanceof LivingEntity) {
                    
                    if (mobValue != null && !mobValue.isEmpty()) {
                        
                        ParameterResolver resolver = new ParameterResolver(context);
                        DataValue resolvedMob = resolver.resolve(context, mobValue);
                        
                        
                        String mobName = resolvedMob.asString();
                        if (mobName != null && !mobName.isEmpty()) {
                            try {
                                EntityType mobType = EntityType.valueOf(mobName.toUpperCase());
                                if (entity.getType() == mobType) {
                                    return true;
                                }
                            } catch (IllegalArgumentException e) {
                                context.getPlugin().getLogger().warning("MobNearCondition: Invalid mob type '" + mobName + "'.");
                                
                            }
                        }
                    } else {
                        
                        return true;
                    }
                }
            }
            
            return false;
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in MobNearCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold mob parameters
     */
    private static class MobNearParams {
        String mobStr = "";
        String distanceStr = "";
    }
}