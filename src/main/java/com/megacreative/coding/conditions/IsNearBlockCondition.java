package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Условие для проверки наличия определенного блока рядом с игроком.
 */
public class IsNearBlockCondition implements BlockCondition {
    
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawMaterial = block.getParameter("material");
        DataValue rawRadius = block.getParameter("radius");

        if (rawMaterial == null) {
            context.getPlugin().getLogger().warning("Material not specified in IsNearBlockCondition");
            return false;
        }

        DataValue materialValue = resolver.resolve(context, rawMaterial);
        String materialName = materialValue.asString();

        int radius = 3; // Default radius
        if (rawRadius != null) {
            DataValue radiusValue = resolver.resolve(context, rawRadius);
            try {
                radius = Integer.parseInt(radiusValue.asString());
            } catch (NumberFormatException e) {
                // Use default radius
            }
        }

        try {
            Material material = Material.valueOf(materialName.toUpperCase());
            Location playerLocation = player.getLocation();
            
            // Check blocks in a cube around the player
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        Location checkLocation = playerLocation.clone().add(x, y, z);
                        if (checkLocation.getBlock().getType() == material) {
                            return true;
                        }
                    }
                }
            }
            
            return false;
        } catch (IllegalArgumentException e) {
            context.getPlugin().getLogger().warning("Invalid material in IsNearBlockCondition: " + materialName);
            return false;
        }
    }
}