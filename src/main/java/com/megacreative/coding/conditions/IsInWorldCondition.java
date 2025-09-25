package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.World;

/**
 * Condition for checking if a player is in a specific world.
 * This condition returns true if the player is in the specified world, false otherwise.
 */
@BlockMeta(id = "isInWorld", displayName = "§aIs In World", type = BlockType.CONDITION)
public class IsInWorldCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get world parameter
            com.megacreative.coding.values.DataValue worldValue = block.getParameter("world");
            if (worldValue == null || worldValue.isEmpty()) {
                return false;
            }
            
            String worldName = worldValue.asString();
            World playerWorld = player.getWorld();
            
            // Check if player is in the specified world
            return playerWorld.getName().equals(worldName);
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}