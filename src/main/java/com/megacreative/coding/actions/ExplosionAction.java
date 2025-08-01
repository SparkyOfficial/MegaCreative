package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExplosionAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return;
        
        Object rawPower = block.getParameter("power");
        Object rawBreakBlocks = block.getParameter("breakBlocks");
        
        String powerStr = ParameterResolver.resolve(context, rawPower).toString();
        String breakBlocksStr = ParameterResolver.resolve(context, rawBreakBlocks).toString();
        
        try {
            float power = Float.parseFloat(powerStr);
            boolean breakBlocks = Boolean.parseBoolean(breakBlocksStr);
            
            Location explosionLocation = player.getLocation();
            player.getWorld().createExplosion(explosionLocation, power, breakBlocks);
            
            player.sendMessage("§a✓ Создан взрыв мощностью " + power);
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: мощность взрыва должна быть числом!");
        }
    }
} 