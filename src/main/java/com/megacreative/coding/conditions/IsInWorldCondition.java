package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class IsInWorldCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawWorld = block.getParameter("world");

        if (rawWorld == null) {
            context.getPlugin().getLogger().warning("World not specified in IsInWorldCondition");
            return false;
        }

        DataValue worldValue = resolver.resolve(context, rawWorld);
        String worldName = worldValue.asString();

        if (worldName == null || worldName.isEmpty()) {
            return false;
        }

        return player.getWorld().getName().equals(worldName);
    }
}