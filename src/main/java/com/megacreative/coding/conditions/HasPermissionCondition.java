package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

public class HasPermissionCondition implements BlockCondition {
    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawPermission = block.getParameter("permission");

        if (rawPermission == null) {
            context.getPlugin().getLogger().warning("Permission not specified in HasPermissionCondition");
            return false;
        }

        DataValue permissionValue = resolver.resolve(context, rawPermission);
        String permission = permissionValue.asString();

        if (permission == null || permission.isEmpty()) {
            return false;
        }

        return player.hasPermission(permission);
    }
}