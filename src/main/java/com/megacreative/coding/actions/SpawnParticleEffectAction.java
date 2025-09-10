package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SpawnParticleEffectAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the block
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
            
            String particleType = resolvedParticle.asString().toUpperCase();
            int count = resolvedCount.asNumber().intValue();
            double spread = resolvedSpread.asNumber().doubleValue();
            double speed = resolvedSpeed.asNumber().doubleValue();
            
            // Validate particle type
            Particle particle;
            try {
                particle = Particle.valueOf(particleType);
            } catch (IllegalArgumentException e) {
                return ExecutionResult.error("Invalid particle type: " + particleType);
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