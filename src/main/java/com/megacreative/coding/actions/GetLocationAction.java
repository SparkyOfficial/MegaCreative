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
 * Action to get a saved location from a variable
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "getLocation", displayName = "§bGet Location", type = BlockType.ACTION)
public class GetLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue locationNameValue = block.getParameter("locationName");
            DataValue targetVariableValue = block.getParameter("targetVariable");
            
            if (locationNameValue == null || targetVariableValue == null) {
                return ExecutionResult.error("Missing required parameters: locationName, targetVariable");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedLocationName = resolver.resolve(context, locationNameValue);
            DataValue resolvedTargetVariable = resolver.resolve(context, targetVariableValue);
            
            String locationName = resolvedLocationName.asString();
            String targetVariable = resolvedTargetVariable.asString();
            
            // Get saved location from variable
            Object locationObj = context.getVariable(locationName);
            if (locationObj == null) {
                return ExecutionResult.error("Location not found: " + locationName);
            }
            
            String locationStr = locationObj.toString();
            if (locationStr.isEmpty()) {
                return ExecutionResult.error("Location not found: " + locationName);
            }
            
            // Parse location string
            String[] parts = locationStr.split(",");
            if (parts.length < 4) {
                return ExecutionResult.error("Invalid location format: " + locationStr);
            }
            
            String worldName = parts[0];
            double x = Double.parseDouble(parts[1]);
            double y = Double.parseDouble(parts[2]);
            double z = Double.parseDouble(parts[3]);
            float yaw = parts.length > 4 ? Float.parseFloat(parts[4]) : 0.0f;
            float pitch = parts.length > 5 ? Float.parseFloat(parts[5]) : 0.0f;
            
            // Create location object
            Location location = new Location(context.getPlugin().getServer().getWorld(worldName), x, y, z, yaw, pitch);
            
            // Save location object to target variable
            context.setVariable(targetVariable, location);
            
            return ExecutionResult.success("Retrieved location " + locationName + " and stored in " + targetVariable);
        } catch (Exception e) {
            return ExecutionResult.error("Failed to get location: " + e.getMessage());
        }
    }
}