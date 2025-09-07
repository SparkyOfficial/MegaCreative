package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class PlayParticleAction implements BlockAction {
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();

        if (player == null) {
            return ExecutionResult.error("Player not available");
        }

        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawParticle = block.getParameter("particle");
        DataValue rawCount = block.getParameter("count");

        if (rawParticle == null) {
            return ExecutionResult.error("Particle type not specified");
        }

        DataValue particleValue = resolver.resolve(context, rawParticle);
        String particleName = particleValue.asString();

        int count = 10; // Default count
        if (rawCount != null) {
            DataValue countValue = resolver.resolve(context, rawCount);
            try {
                count = Integer.parseInt(countValue.asString());
            } catch (NumberFormatException e) {
                // Use default count
            }
        }

        try {
            Particle particle = Particle.valueOf(particleName.toUpperCase());
            player.getWorld().spawnParticle(particle, player.getLocation(), count);
            return ExecutionResult.success("Played particle: " + particleName);
        } catch (IllegalArgumentException e) {
            return ExecutionResult.error("Invalid particle type: " + particleName);
        }
    }
}