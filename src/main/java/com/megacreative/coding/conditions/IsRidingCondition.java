package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is riding a specific entity.
 * This condition returns true if the player is riding the specified entity type.
 */
@BlockMeta(id = "isRiding", displayName = "§aIs Riding", type = BlockType.CONDITION)
public class IsRidingCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue entityValue = block.getParameter("entity");
            
            // If no entity is specified, just check if player is riding anything
            if (entityValue == null || entityValue.isEmpty()) {
                return player.isInsideVehicle();
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedEntity = resolver.resolve(context, entityValue);
            
            String entityName = resolvedEntity.asString();
            
            if (entityName.isEmpty()) {
                return player.isInsideVehicle();
            }

            
            if (!player.isInsideVehicle()) {
                return false;
            }

            
            try {
                EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                return player.getVehicle().getType() == entityType;
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("IsRidingCondition: Invalid entity type '" + entityName + "'.");
                return false;
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error in IsRidingCondition: " + e.getMessage());
            return false;
        }
    }
}