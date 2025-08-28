package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class PlayParticleEffectAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        VariableManager variableManager = context.getPlugin().getVariableManager();

        if (player == null || block == null || variableManager == null) return;

        ParameterResolver resolver = new ParameterResolver(variableManager);

        // Получаем и разрешаем параметры
        DataValue rawParticle = block.getParameter("particle");
        DataValue rawCount = block.getParameter("count");
        DataValue rawOffset = block.getParameter("offset");

        if (rawParticle == null) return;

        DataValue particleValue = resolver.resolve(context, rawParticle);
        String particleStr = particleValue.asString();
        
        String countStr = null;
        String offsetStr = null;
        
        if (rawCount != null) {
            DataValue countValue = resolver.resolve(context, rawCount);
            countStr = countValue.asString();
        }
        
        if (rawOffset != null) {
            DataValue offsetValue = resolver.resolve(context, rawOffset);
            offsetStr = offsetValue.asString();
        }

        if (particleStr == null) return;

        try {
            Particle particle = Particle.valueOf(particleStr.toUpperCase());
            int count = countStr != null ? Integer.parseInt(countStr) : 10;
            double offset = offsetStr != null ? Double.parseDouble(offsetStr) : 0.5;
            
            Location location = context.getBlockLocation() != null ? 
                context.getBlockLocation() : player.getLocation();
            
            location.getWorld().spawnParticle(particle, location, count, offset, offset, offset);
            
            player.sendMessage("§a✨ Эффект частиц '" + particleStr + "' воспроизведен!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка в параметрах count/offset");
        } catch (IllegalArgumentException e) {
            player.sendMessage("§cНеизвестный тип частиц: " + particleStr);
        }
    }
} 