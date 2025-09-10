package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Condition for checking if a player is near a specific block.
 * This condition returns true if the player is within a specified distance of a block type.
 */
public class IsNearBlockCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get the block type parameter from the block
            DataValue blockValue = block.getParameter("block");
            if (blockValue == null) {
                return false;
            }

            // Resolve any placeholders in the block type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedBlock = resolver.resolve(context, blockValue);
            
            // Parse block type parameter
            String blockName = resolvedBlock.asString();
            if (blockName == null || blockName.isEmpty()) {
                return false;
            }

            // Get optional distance parameter (default to 5)
            int distance = 5;
            DataValue distanceValue = block.getParameter("distance");
            if (distanceValue != null) {
                DataValue resolvedDistance = resolver.resolve(context, distanceValue);
                try {
                    distance = Math.max(1, resolvedDistance.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default distance if parsing fails
                }
            }

            // Check if player is near the specified block type
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                Location playerLocation = player.getLocation();
                
                // Check blocks in a cube around the player
                for (int x = -distance; x <= distance; x++) {
                    for (int y = -distance; y <= distance; y++) {
                        for (int z = -distance; z <= distance; z++) {
                            Block nearbyBlock = playerLocation.getBlock().getRelative(x, y, z);
                            if (nearbyBlock.getType() == material) {
                                return true;
                            }
                        }
                    }
                }
                
                return false;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}