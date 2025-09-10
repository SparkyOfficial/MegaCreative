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
 * Condition for checking if a block at a location is of a specific type.
 * This condition returns true if the block at the specified location is of the specified type.
 */
public class IsBlockTypeCondition implements BlockCondition {

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

            // Get optional relative X parameter (default to 0)
            int relativeX = 0;
            DataValue relativeXValue = block.getParameter("relativeX");
            if (relativeXValue != null) {
                try {
                    relativeX = relativeXValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default relativeX if parsing fails
                }
            }

            // Get optional relative Y parameter (default to 0)
            int relativeY = 0;
            DataValue relativeYValue = block.getParameter("relativeY");
            if (relativeYValue != null) {
                try {
                    relativeY = relativeYValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default relativeY if parsing fails
                }
            }

            // Get optional relative Z parameter (default to 0)
            int relativeZ = 0;
            DataValue relativeZValue = block.getParameter("relativeZ");
            if (relativeZValue != null) {
                try {
                    relativeZ = relativeZValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default relativeZ if parsing fails
                }
            }

            // Resolve any placeholders in the block type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedBlock = resolver.resolve(context, blockValue);
            
            // Parse block type parameter
            String blockName = resolvedBlock.asString();
            if (blockName == null || blockName.isEmpty()) {
                return false;
            }

            // Check if the block at the specified location is of the specified type
            try {
                Material material = Material.valueOf(blockName.toUpperCase());
                Location location = player.getLocation().add(relativeX, relativeY, relativeZ);
                Block checkBlock = location.getBlock();
                
                return checkBlock.getType() == material;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
}