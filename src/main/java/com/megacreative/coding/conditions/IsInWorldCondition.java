package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a player is in a specific world.
 * This condition returns true if the player is in the specified world.
 */
public class IsInWorldCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the world name parameter from the block
            DataValue worldValue = block.getParameter("world");
            if (worldValue == null) {
                return false;
            }

            // Resolve any placeholders in the world name
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedWorld = resolver.resolve(context, worldValue);
            
            // Parse world name parameter
            String worldName = resolvedWorld.asString();
            if (worldName == null || worldName.isEmpty()) {
                return false;
            }

            // Check if player is in the specified world
            return player.getWorld().getName().equals(worldName);
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}