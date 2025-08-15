package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.List;

public class MobNearCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        // Получаем и разрешаем параметры
        Object rawMobType = block.getParameter("mob");
        Object rawRadius = block.getParameter("radius");

        String mobTypeStr = ParameterResolver.resolve(context, rawMobType);
        String radiusStr = ParameterResolver.resolve(context, rawRadius);

        if (radiusStr == null) return false;

        try {
            int radius = Integer.parseInt(radiusStr);
            List<org.bukkit.entity.Entity> nearbyEntities = player.getNearbyEntities(radius, radius, radius);

            // Если не указан тип моба, проверяем наличие любого моба
            if (mobTypeStr == null || mobTypeStr.isEmpty()) {
                return !nearbyEntities.isEmpty();
            }

            // Проверяем наличие конкретного типа моба
            EntityType requiredType = EntityType.valueOf(mobTypeStr.toUpperCase());
            return nearbyEntities.stream()
                    .anyMatch(entity -> entity.getType() == requiredType);

        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка в параметрах моба: " + mobTypeStr);
            return false;
        }
    }
} 