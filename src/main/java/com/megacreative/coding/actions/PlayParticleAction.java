package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Action for playing a particle effect.
 * This action plays a particle effect at the player's location.
 */
public class PlayParticleAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the particle type parameter from the block
            DataValue particleValue = block.getParameter("particle");
            if (particleValue == null) {
                return ExecutionResult.error("Particle type parameter is missing");
            }

            // Get the count parameter from the block (default to 10)
            int count = 10;
            DataValue countValue = block.getParameter("count");
            if (countValue != null) {
                try {
                    count = Math.max(1, countValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default count if parsing fails
                }
            }

            // Get the speed parameter from the block (default to 0.1)
            double speed = 0.1;
            DataValue speedValue = block.getParameter("speed");
            if (speedValue != null) {
                try {
                    speed = Math.max(0, speedValue.asNumber().doubleValue());
                } catch (NumberFormatException e) {
                    // Use default speed if parsing fails
                }
            }

            // Resolve any placeholders in the particle type
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedParticle = resolver.resolve(context, particleValue);
            
            // Parse particle type parameter
            String particleName = resolvedParticle.asString();
            if (particleName == null || particleName.isEmpty()) {
                return ExecutionResult.error("Particle type is empty or null");
            }

            // Play the particle effect
            try {
                Particle particle = Particle.valueOf(particleName.toUpperCase());
                Location location = player.getLocation();
                
                player.getWorld().spawnParticle(particle, location, count, 0.5, 0.5, 0.5, speed);
                
                return ExecutionResult.success("Played particle effect: " + particle.name());
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid particle type: " + particleName);
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to play particle: " + e.getMessage());
        }
    }
}