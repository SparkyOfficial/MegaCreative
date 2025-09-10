package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is riding a specific entity.
 * This condition returns true if the player is riding the specified entity type.
 */
public class IsRidingCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the entity type parameter from the block (optional)
            DataValue entityValue = block.getParameter("entity");
            
            // If a specific entity type is provided, check for that type
            if (entityValue != null) {
                ParameterResolver resolver = new ParameterResolver(context);
                DataValue resolvedEntity = resolver.resolve(context, entityValue);
                
                String entityName = resolvedEntity.asString();
                if (entityName == null || entityName.isEmpty()) {
                    return false;
                }
                
                try {
                    EntityType entityType = EntityType.valueOf(entityName.toUpperCase());
                    return player.isInsideVehicle() && player.getVehicle().getType() == entityType;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            } else {
                // If no entity type is specified, just check if the player is riding anything
                return player.isInsideVehicle();
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}