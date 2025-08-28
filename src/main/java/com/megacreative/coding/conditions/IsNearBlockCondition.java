package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class IsNearBlockCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return false;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        try {
            // Получаем и разрешаем параметры
            DataValue rawBlockType = block.getParameter("block");
            DataValue rawRadius = block.getParameter("radius");

            if (rawBlockType == null || rawRadius == null) return false;

            DataValue blockTypeValue = resolver.resolve(context, rawBlockType);
            DataValue radiusValue = resolver.resolve(context, rawRadius);

            String blockTypeStr = blockTypeValue.asString();
            String radiusStr = radiusValue.asString();

            if (blockTypeStr == null || radiusStr == null) return false;

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
            player.sendMessage("§cОшибка в параметрах блока: " + (block.getParameter("block") != null ? block.getParameter("block").asString() : "null"));
            return false;
        }
    }
} 