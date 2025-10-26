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
 * Action to spawn advanced particle effects
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "spawnParticleEffect", displayName = "§bSpawn Particle Effect", type = BlockType.ACTION)
public class SpawnParticleEffectAction implements BlockAction {
    
    
    private static final int MAX_PARTICLES = 1000;
    
    private static final double MAX_SPREAD = 10.0;
    
    private static final double MAX_SPEED = 5.0;
    
    private static final double MAX_OFFSET = 10.0;

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
            DataValue spreadValue = block.getParameter("spread");
            DataValue speedValue = block.getParameter("speed");
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
            
            // Get spread (default to 1.0)
            double spread = 1.0;
            if (spreadValue != null) {
                DataValue resolvedSpread = resolver.resolve(context, spreadValue);
                spread = Math.max(0.0, resolvedSpread.asNumber().doubleValue());
            }
            
            // Get speed (default to 1.0)
            double speed = 1.0;
            if (speedValue != null) {
                DataValue resolvedSpeed = resolver.resolve(context, speedValue);
                speed = Math.max(0.0, resolvedSpeed.asNumber().doubleValue());
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
            
            // Spawn particle effect
            player.getWorld().spawnParticle(particle, location, count, spread, spread, spread, speed);
            
            return ExecutionResult.success("Spawned particle effect " + particle.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to spawn particle effect: " + e.getMessage());
        }
    }
}