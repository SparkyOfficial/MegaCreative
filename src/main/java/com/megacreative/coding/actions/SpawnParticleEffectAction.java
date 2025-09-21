package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Action to spawn particle effects with configurable parameters.
 * Includes safety limits to prevent server lag.
 */
public class SpawnParticleEffectAction implements BlockAction {
    
    // Maximum number of particles that can be spawned at once to prevent lag
    private static final int MAX_PARTICLES = 1000;
    // Maximum spread value to prevent excessive particle spread
    private static final double MAX_SPREAD = 10.0;
    // Maximum speed value to prevent particles from moving too fast
    private static final double MAX_SPEED = 5.0;

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        if (block == null || context == null) {
            return ExecutionResult.error("CodeBlock and ExecutionContext cannot be null");
        }
        
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block with default values
            DataValue particleValue = block.getParameter("particle");
            DataValue countValue = block.getParameter("count", DataValue.of(10));
            DataValue spreadValue = block.getParameter("spread", DataValue.of(1.0));
            DataValue speedValue = block.getParameter("speed", DataValue.of(0.1));
            
            if (particleValue == null || particleValue.isEmpty()) {
                return ExecutionResult.error("Particle type parameter is missing.");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedParticle = resolver.resolve(context, particleValue);
            DataValue resolvedCount = resolver.resolve(context, countValue);
            DataValue resolvedSpread = resolver.resolve(context, spreadValue);
            DataValue resolvedSpeed = resolver.resolve(context, speedValue);
            
            if (resolvedParticle == null || resolvedParticle.isEmpty()) {
                return ExecutionResult.error("Could not resolve particle type.");
            }
            
            String particleType = resolvedParticle.asString().toUpperCase();
            
            // Validate particle type
            Particle particle;
            try {
                particle = Particle.valueOf(particleType);
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid particle type: " + particleType);
            }
            
            // Validate and sanitize count
            int count;
            try {
                count = resolvedCount.asNumber().intValue();
                if (count < 0) {
                    return ExecutionResult.error("Particle count cannot be negative.");
                }
                if (count > MAX_PARTICLES) {
                    context.getPlugin().getLogger().warning("Particle count " + count + " exceeds maximum of " + MAX_PARTICLES + ". Clamping to maximum.");
                    count = MAX_PARTICLES;
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid particle count: " + resolvedCount.asString());
            }
            
            // Validate and sanitize spread
            double spread;
            try {
                spread = Math.abs(resolvedSpread.asNumber().doubleValue());
                if (spread > MAX_SPREAD) {
                    context.getPlugin().getLogger().warning("Particle spread " + spread + " exceeds maximum of " + MAX_SPREAD + ". Clamping to maximum.");
                    spread = MAX_SPREAD;
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid spread value: " + resolvedSpread.asString());
            }
            
            // Validate and sanitize speed
            double speed;
            try {
                speed = Math.abs(resolvedSpeed.asNumber().doubleValue());
                if (speed > MAX_SPEED) {
                    context.getPlugin().getLogger().warning("Particle speed " + speed + " exceeds maximum of " + MAX_SPEED + ". Clamping to maximum.");
                    speed = MAX_SPEED;
                }
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid speed value: " + resolvedSpeed.asString());
            }
            
            // Spawn particles
            player.getWorld().spawnParticle(
                particle,
                player.getLocation(),
                count,
                spread,
                spread,
                spread,
                speed
            );
            
            return ExecutionResult.success("Particle effect created.");

        } catch (Exception e) {
            return ExecutionResult.error("Error creating particle effect: " + e.getMessage());
        }
    }
}