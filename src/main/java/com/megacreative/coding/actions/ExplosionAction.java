package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExplosionAction implements BlockAction {
    @Override
    public void execute(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();

        if (player == null || block == null) return;

        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return;
        
        ParameterResolver resolver = new ParameterResolver(context);

        // Получаем и разрешаем параметры
        DataValue rawPower = block.getParameter("power");
        DataValue rawBreakBlocks = block.getParameter("breakBlocks");

        String powerStr = rawPower != null ? resolver.resolve(context, rawPower).asString() : "4.0";
        String breakBlocksStr = rawBreakBlocks != null ? resolver.resolve(context, rawBreakBlocks).asString() : "true";

        try {
            float power = powerStr != null ? Float.parseFloat(powerStr) : 4.0f;
            boolean breakBlocks = breakBlocksStr != null ? Boolean.parseBoolean(breakBlocksStr) : true;
            
            Location location = context.getBlockLocation() != null ? 
                context.getBlockLocation() : player.getLocation();
            
            location.getWorld().createExplosion(location, power, breakBlocks);
            player.sendMessage("§a💥 Взрыв создан с мощностью " + power + "!");
            
        } catch (NumberFormatException e) {
            player.sendMessage("§cОшибка: мощность должна быть числом");
        }
    }
} 