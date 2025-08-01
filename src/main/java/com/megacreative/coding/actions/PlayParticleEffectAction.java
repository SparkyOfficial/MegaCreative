package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class PlayParticleEffectAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        // Получаем и разрешаем параметры
        Object rawParticle = block.getParameter("particle");
        Object rawCount = block.getParameter("count");
        Object rawOffset = block.getParameter("offset");

        String particleStr = ParameterResolver.resolve(context, rawParticle);
        String countStr = ParameterResolver.resolve(context, rawCount);
        String offsetStr = ParameterResolver.resolve(context, rawOffset);

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