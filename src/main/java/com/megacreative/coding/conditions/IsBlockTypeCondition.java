package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.Location;

/**
 * Condition for checking if a block at a relative position is of a specific type from the new parameter system.
 * This condition returns true if the block at the specified relative position is of the specified type.
 */
@BlockMeta(id = "isBlockType", displayName = "§aIs Block Type", type = BlockType.CONDITION)
public class IsBlockTypeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the new parameter system
            DataValue blockValue = block.getParameter("block");
            DataValue relativeXValue = block.getParameter("relativeX");
            DataValue relativeYValue = block.getParameter("relativeY");
            DataValue relativeZValue = block.getParameter("relativeZ");
            
            if (blockValue == null || blockValue.isEmpty()) {
                context.getPlugin().getLogger().warning("IsBlockTypeCondition: 'block' parameter is missing.");
                return false;
            }

            // Parse relative coordinates (default to 0)
            int relativeX = 0, relativeY = 0, relativeZ = 0;
            
            if (relativeXValue != null && !relativeXValue.isEmpty()) {
                try {
                    relativeX = relativeXValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("IsBlockTypeCondition: Invalid relativeX value, using default 0.");
                }
            }
            
            if (relativeYValue != null && !relativeYValue.isEmpty()) {
                try {
                    relativeY = relativeYValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("IsBlockTypeCondition: Invalid relativeY value, using default 0.");
                }
            }
            
            if (relativeZValue != null && !relativeZValue.isEmpty()) {
                try {
                    relativeZ = relativeZValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    context.getPlugin().getLogger().warning("IsBlockTypeCondition: Invalid relativeZ value, using default 0.");
                }
            }

            // Resolve any placeholders in the block type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedBlock = resolver.resolve(context, blockValue);
            
            // Parse block type parameter
            String blockName = resolvedBlock.asString();
            if (blockName == null || blockName.isEmpty()) {
                context.getPlugin().getLogger().warning("IsBlockTypeCondition: 'block' parameter is empty.");
                return false;
            }

            // Get the block at the relative position
            Location playerLocation = player.getLocation();
            Location blockLocation = playerLocation.clone().add(relativeX, relativeY, relativeZ);
            Block targetBlock = blockLocation.getBlock();

            // Check if the block is of the specified type
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                return targetBlock.getType() == material;
            } catch (IllegalArgumentException e) {
                context.getPlugin().getLogger().warning("IsBlockTypeCondition: Invalid block material '" + blockName + "'.");
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            context.getPlugin().getLogger().warning("Error in IsBlockTypeCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold block parameters
     */
    private static class IsBlockTypeParams {
        String blockStr = "";
        String relativeXStr = "";
        String relativeYStr = "";
        String relativeZStr = "";
    }
}