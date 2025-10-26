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
 * Action to create an explosion
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "explosion", displayName = "§bCreate Explosion", type = BlockType.ACTION)
public class ExplosionAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue powerValue = block.getParameter("power");
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            
            // Get power (default to 4.0)
            float power = 4.0f;
            if (powerValue != null) {
                DataValue resolvedPower = resolver.resolve(context, powerValue);
                power = Math.max(0.0f, resolvedPower.asNumber().floatValue());
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
            
            // Create explosion
            boolean success = player.getWorld().createExplosion(location, power);
            
            if (success) {
                return ExecutionResult.success("Created explosion with power " + power);
            } else {
                return ExecutionResult.error("Failed to create explosion");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create explosion: " + e.getMessage());
        }
    }
}