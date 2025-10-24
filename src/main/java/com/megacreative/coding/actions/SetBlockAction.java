package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Action for setting a block at a location.
 * This action retrieves block parameters from the new parameter system and sets the block.
 */
@BlockMeta(id = "setBlock", displayName = "§aSet Block", type = BlockType.ACTION)
public class SetBlockAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            
            DataValue blockValue = block.getParameter("block");
            DataValue relativeXValue = block.getParameter("relativeX");
            DataValue relativeYValue = block.getParameter("relativeY");
            DataValue relativeZValue = block.getParameter("relativeZ");
            
            if (blockValue == null || blockValue.isEmpty()) {
                return ExecutionResult.error("No block provided");
            }

            
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedBlockName = resolver.resolve(context, blockValue);
            
            
            String blockName = resolvedBlockName.asString();
            int relativeX = 0;
            int relativeY = 0;
            int relativeZ = 0;
            
            if (relativeXValue != null && !relativeXValue.isEmpty()) {
                try {
                    relativeX = Integer.parseInt(relativeXValue.asString());
                } catch (NumberFormatException e) {
                    // Log the error but continue with default value of 0
                    context.getPlugin().getLogger().warning("NumberFormatException in SetBlockAction: " + e.getMessage());
                }
            }
            
            if (relativeYValue != null && !relativeYValue.isEmpty()) {
                try {
                    relativeY = Integer.parseInt(relativeYValue.asString());
                } catch (NumberFormatException e) {
                    // Log the error but continue with default value of 0
                    context.getPlugin().getLogger().warning("NumberFormatException in SetBlockAction: " + e.getMessage());
                }
            }
            
            if (relativeZValue != null && !relativeZValue.isEmpty()) {
                try {
                    relativeZ = Integer.parseInt(relativeZValue.asString());
                } catch (NumberFormatException e) {
                    // Log the error but continue with default value of 0
                    context.getPlugin().getLogger().warning("NumberFormatException in SetBlockAction: " + e.getMessage());
                }
            }

            
            Material material;
            try {
                material = Material.valueOf(blockName.toUpperCase());
            } catch (IllegalArgumentException e) {
                
                material = Material.STONE;
            }

            
            Location playerLocation = player.getLocation();
            Location targetLocation = playerLocation.clone().add(relativeX, relativeY, relativeZ);
            
            
            targetLocation.getBlock().setType(material);
            
            context.getPlugin().getLogger().fine("Setting block " + material + " at relative position (" + relativeX + ", " + relativeY + ", " + relativeZ + ")");
            
            return ExecutionResult.success("Block set successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set block: " + e.getMessage());
        }
    }
}