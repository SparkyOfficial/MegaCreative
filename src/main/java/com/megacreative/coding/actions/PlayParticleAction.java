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
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Action to play particle effects
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "playParticle", displayName = "§bPlay Particle", type = BlockType.ACTION)
public class PlayParticleAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue particleValue = block.getParameter("particle");
            DataValue countValue = block.getParameter("count");
            DataValue xValue = block.getParameter("x");
            DataValue yValue = block.getParameter("y");
            DataValue zValue = block.getParameter("z");
            
            if (particleValue == null) {
                return ExecutionResult.error("Missing required parameter: particle");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedParticle = resolver.resolve(context, particleValue);
            
            String particleStr = resolvedParticle.asString();
            Particle particle;
            
            try {
                particle = Particle.valueOf(particleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid particle type: " + particleStr);
            }
            
            // Get count (default to 10)
            int count = 10;
            if (countValue != null) {
                DataValue resolvedCount = resolver.resolve(context, countValue);
                count = Math.max(1, resolvedCount.asNumber().intValue());
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
            
            // Play particle
            player.getWorld().spawnParticle(particle, location, count);
            
            return ExecutionResult.success("Played particle " + particle.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play particle: " + e.getMessage());
        }
    }
}