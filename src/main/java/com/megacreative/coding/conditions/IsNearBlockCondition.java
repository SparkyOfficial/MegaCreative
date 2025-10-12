package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Condition for checking if a specific block type is near the player from the new parameter system.
 * This condition returns true if the specified block type is within a specified distance of the player.
 */
@BlockMeta(id = "isNearBlock", displayName = "§aIs Near Block", type = BlockType.CONDITION)
public class IsNearBlockCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            
            DataValue blockValue = block.getParameter("block");
            DataValue distanceValue = block.getParameter("distance");
            
            
            if (blockValue == null || blockValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IsNearBlockCondition: 'block' parameter is missing.");
                return false;
            }
            
            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedBlock = resolver.resolve(context, blockValue);
            
            String blockName = resolvedBlock.asString();
            if (blockName == null || blockName.isEmpty()) {
                context.getPlugin().getLogger().warning("IsNearBlockCondition: 'block' parameter is empty.");
                return false;
            }

            
            int distance = 5;
            if (distanceValue != null && !distanceValue.isEmpty()) {
                try {
                    distance = Math.max(1, distanceValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("IsNearBlockCondition: Invalid distance value, using default 5.");
                }
            }

            
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                
                
                for (int x = -distance; x <= distance; x++) {
                    for (int y = -distance; y <= distance; y++) {
                        for (int z = -distance; z <= distance; z++) {
                            if (player.getLocation().getBlock().getRelative(x, y, z).getType() == material) {
                                return true;
                            }
                        }
                    }
                }
                
                return false;
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("IsNearBlockCondition: Invalid block material '" + blockName + "'.");
                return false;
            }
        } catch (Exception e) {
            
            context.getPlugin().getLogger().warning("Error in IsNearBlockCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold block parameters
     */
    private static class IsNearBlockParams {
        String blockStr = "";
        String distanceStr = "";
    }
}