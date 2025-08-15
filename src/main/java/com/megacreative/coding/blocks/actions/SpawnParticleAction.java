package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

public class SpawnParticleAction implements BlockAction {
    
    private final Argument<TextValue> particleTypeArgument;
    private final Argument<TextValue> countArgument;
    private final Argument<TextValue> offsetXArgument;
    private final Argument<TextValue> offsetYArgument;
    private final Argument<TextValue> offsetZArgument;
    
    public SpawnParticleAction() {
        this.particleTypeArgument = new ParameterArgument("particleType");
        this.countArgument = new ParameterArgument("count");
        this.offsetXArgument = new ParameterArgument("offsetX");
        this.offsetYArgument = new ParameterArgument("offsetY");
        this.offsetZArgument = new ParameterArgument("offsetZ");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Получаем параметры
        TextValue particleTypeValue = particleTypeArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue countValue = countArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue offsetXValue = offsetXArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue offsetYValue = offsetYArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue offsetZValue = offsetZArgument.parse(context.getCurrentBlock()).orElse(null);
        
        if (particleTypeValue == null) {
            player.sendMessage("§cОшибка: не указан тип частиц!");
            return;
        }
        
        try {
            String particleType = particleTypeValue.get(context);
            int count = countValue != null ? Integer.parseInt(countValue.get(context)) : 10;
            double offsetX = offsetXValue != null ? Double.parseDouble(offsetXValue.get(context)) : 0.5;
            double offsetY = offsetYValue != null ? Double.parseDouble(offsetYValue.get(context)) : 0.5;
            double offsetZ = offsetZValue != null ? Double.parseDouble(offsetZValue.get(context)) : 0.5;
            
            Particle particle = Particle.valueOf(particleType.toUpperCase());
            Location location = player.getLocation();
            
            player.getWorld().spawnParticle(particle, location, count, offsetX, offsetY, offsetZ);
            player.sendMessage("§a✓ Создано " + count + " частиц " + particleType);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка: " + e.getMessage());
        }
    }
} 