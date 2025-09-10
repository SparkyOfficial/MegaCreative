package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.block.Block;

/**
 * Action for setting a block.
 * This action sets a block at the player's location or a specified location.
 */
public class SetBlockAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the material parameter from the block
            DataValue materialValue = block.getParameter("material");
            if (materialValue == null) {
                return ExecutionResult.error("Material parameter is missing");
            }

            // Get the relative X parameter from the block (default to 0)
            int relativeX = 0;
            DataValue relativeXValue = block.getParameter("relativeX");
            if (relativeXValue != null) {
                try {
                    relativeX = relativeXValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default relativeX if parsing fails
                }
            }

            // Get the relative Y parameter from the block (default to 0)
            int relativeY = 0;
            DataValue relativeYValue = block.getParameter("relativeY");
            if (relativeYValue != null) {
                try {
                    relativeY = relativeYValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default relativeY if parsing fails
                }
            }

            // Get the relative Z parameter from the block (default to 0)
            int relativeZ = 0;
            DataValue relativeZValue = block.getParameter("relativeZ");
            if (relativeZValue != null) {
                try {
                    relativeZ = relativeZValue.asNumber().intValue();
                } catch (NumberFormatException e) {
                    // Use default relativeZ if parsing fails
                }
            }

            // Resolve any placeholders in the material
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedMaterial = resolver.resolve(context, materialValue);
            
            // Parse material parameter
            String materialName = resolvedMaterial.asString();
            if (materialName == null || materialName.isEmpty()) {
                return ExecutionResult.error("Material is empty or null");
            }

            // Set the block
            try {
                Material material = Material.valueOf(materialName.toUpperCase());
                Location location = player.getLocation().add(relativeX, relativeY, relativeZ);
                Block blockToSet = location.getBlock();
                blockToSet.setType(material);
                
                return ExecutionResult.success("Set block to " + material.name() + " at " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid material: " + materialName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set block: " + e.getMessage());
        }
    }
}