package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player has a specific permission.
 * This condition retrieves a permission string from the block parameters and checks if the player has it.
 */
public class HasPermissionCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the permission parameter from the block
            DataValue permissionValue = block.getParameter("permission");
            if (permissionValue == null) {
                return false;
            }

            // Resolve any placeholders in the permission
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedPermission = resolver.resolve(context, permissionValue);
            
            // Check if the player has the permission
            String permission = resolvedPermission.asString();
            if (permission != null && !permission.isEmpty()) {
                return player.hasPermission(permission);
            }
            
            return false;
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}