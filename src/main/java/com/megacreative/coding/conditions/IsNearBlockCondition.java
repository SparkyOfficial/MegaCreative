package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class IsNearBlockCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return false;

        // Получаем и разрешаем параметры
        Object rawBlockType = block.getParameter("block");
        Object rawRadius = block.getParameter("radius");

        String blockTypeStr = ParameterResolver.resolve(context, rawBlockType);
        String radiusStr = ParameterResolver.resolve(context, rawRadius);

        if (blockTypeStr == null || radiusStr == null) return false;

        try {
            Material blockType = Material.valueOf(blockTypeStr.toUpperCase());
            int radius = Integer.parseInt(radiusStr);

            // Проверяем блоки в радиусе
            for (int x = -radius; x <= radius; x++) {
                for (int y = -radius; y <= radius; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        if (player.getLocation().add(x, y, z).getBlock().getType() == blockType) {
                            return true;
                        }
                    }
                }
            }
            return false;
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cОшибка в параметрах блока: " + blockTypeStr);
            return false;
        }
    }
} 