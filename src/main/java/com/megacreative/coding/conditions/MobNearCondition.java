package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

/**
 * Условие для проверки наличия мобов рядом с игроком.
 */
public class MobNearCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawEntityType = block.getParameter("entityType");
        DataValue rawRadius = block.getParameter("radius");

        EntityType entityType = null;
        if (rawEntityType != null) {
            DataValue entityTypeValue = resolver.resolve(context, rawEntityType);
            String entityTypeName = entityTypeValue.asString();
            
            try {
                entityType = EntityType.valueOf(entityTypeName.toUpperCase());
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("Invalid entity type in MobNearCondition: " + entityTypeName);
                return false;
            }
        }

        int radius = 3; // Default radius
        if (rawRadius != null) {
            DataValue radiusValue = resolver.resolve(context, rawRadius);
            try {
                radius = Integer.parseInt(radiusValue.asString());
            } catch (NumberFormatException e) {
                // Use default radius
            }
        }

        Location playerLocation = player.getLocation();
        
        // Check for entities near the player
        for (Entity entity : playerLocation.getWorld().getNearbyEntities(playerLocation, radius, radius, radius)) {
            // If no specific entity type is specified, any entity counts
            if (entityType == null) {
                // Exclude the player themselves
                if (!entity.equals(player)) {
                    return true;
                }
            } else {
                // Check for specific entity type
                if (entity.getType() == entityType) {
                    return true;
                }
            }
        }
        
        return false;
    }
}