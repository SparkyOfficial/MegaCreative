package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CreateExplosionAction implements BlockAction {
    
    private final Argument<TextValue> powerArgument;
    private final Argument<TextValue> setFireArgument;
    private final Argument<TextValue> breakBlocksArgument;
    
    public CreateExplosionAction() {
        this.powerArgument = new ParameterArgument("power");
        this.setFireArgument = new ParameterArgument("setFire");
        this.breakBlocksArgument = new ParameterArgument("breakBlocks");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return;
        
        // Получаем параметры
        TextValue powerValue = powerArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue setFireValue = setFireArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue breakBlocksValue = breakBlocksArgument.parse(context.getCurrentBlock()).orElse(null);
        
        try {
            float power = powerValue != null ? Float.parseFloat(powerValue.get(context)) : 1.0f;
            boolean setFire = setFireValue != null ? Boolean.parseBoolean(setFireValue.get(context)) : false;
            boolean breakBlocks = breakBlocksValue != null ? Boolean.parseBoolean(breakBlocksValue.get(context)) : true;
            
            Location location = player.getLocation();
            
            // Создаем взрыв
            player.getWorld().createExplosion(location, power, setFire, breakBlocks);
            player.sendMessage("§a✓ Создан взрыв мощностью " + power);
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка: " + e.getMessage());
        }
    }
} 