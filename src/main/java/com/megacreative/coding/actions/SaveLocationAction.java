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
import org.bukkit.entity.Player;

/**
 * Action to save a location to a variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "saveLocation", displayName = "§bSave Location", type = BlockType.ACTION)
public class SaveLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue locationNameValue = block.getParameter("locationName");
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            DataValue worldValue = block.getParameter("world");
            
            if (locationNameValue == null) {
                return ExecutionResult.error("Missing required parameter: locationName");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameValue);
            
            String locationName = resolvedLocationName.asString();
            
            // Get location (default to player's location)
            Location location = player.getLocation();
            if (xValue != null && yValue != null && zValue != null) {
                DataValue resolvedX = resolver.resolve(context, xValue);
                DataValue resolvedY = resolver.resolve(context, yValue);
                DataValue resolvedZ = resolver.resolve(context, zValue);
                
                double x = resolvedX.asNumber().doubleValue();
                double y = resolvedY.asNumber().doubleValue();
                double z = resolvedZ.asNumber().doubleValue();
                
                String worldName = player.getWorld().getName();
                if (worldValue != null) {
                    DataValue resolvedWorld = resolver.resolve(context, worldValue);
                    worldName = resolvedWorld.asString();
                }
                
                location = new Location(context.getPlugin().getServer().getWorld(worldName), x, y, z);
            }
            
            // Save location as a string in variable
            String locationStr = location.getWorld().getName() + "," + 
                                location.getX() + "," + 
                                location.getY() + "," + 
                                location.getZ() + "," + 
                                location.getYaw() + "," + 
                                location.getPitch();
            
            context.setVariable(locationName, locationStr);
            
            return ExecutionResult.success("Saved location " + locationName);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to save location: " + e.getMessage());
        }
    }
}