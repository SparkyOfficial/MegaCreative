package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player has a specific permission.
 * This condition returns true if the player has the specified permission, false otherwise.
 */
@BlockMeta(id = "hasPermission", displayName = "§aHas Permission", type = BlockType.CONDITION)
public class HasPermissionCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get permission parameter
            com.megacreative.coding.values.DataValue permissionValue = block.getParameter("permission");
            if (permissionValue == null || permissionValue.isEmpty()) {
                return false;
            }
            
            String permission = permissionValue.asString();
            
            // Check if the player has the permission
            return player.hasPermission(permission);
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}