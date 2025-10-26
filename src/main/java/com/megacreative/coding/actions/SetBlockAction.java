package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

/**
 * Action to set a block at a specific location
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "setBlock", displayName = "§bSet Block", type = BlockType.ACTION)
public class SetBlockAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue blockValue = block.getParameter("block");
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            
            if (blockValue == null) {
                return ExecutionResult.error("Missing required parameter: block");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedBlock = resolver.resolve(context, blockValue);
            
            String blockStr = resolvedBlock.asString();
            Material material = Material.matchMaterial(blockStr);
            
            if (material == null) {
                return ExecutionResult.error("Invalid block material: " + blockStr);
            }
            
            // Get location (default to player's location)
            Location location = player.getLocation();
            if (xValue != null && yValue != null && zValue != null) {
                DataValue resolvedX = resolver.resolve(context, xValue);
                DataValue resolvedY = resolver.resolve(context, yValue);
                DataValue resolvedZ = resolver.resolve(context, zValue);
                
                double x = resolvedX.asNumber().doubleValue();
                double y = resolvedY.asNumber().doubleValue();
                double z = resolvedZ.asNumber().doubleValue();
                
                location = new Location(player.getWorld(), x, y, z);
            }
            
            // Set block
            location.getBlock().setType(material);
            
            return ExecutionResult.success("Set block to " + material.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set block: " + e.getMessage());
        }
    }
}