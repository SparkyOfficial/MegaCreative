package com.megacreative.coding.actions;

import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldTimeCondition implements BlockCondition {
    @Override
    public boolean check(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        Object rawTimeRange = block.getParameter("timeRange");
        String timeRange = ParameterResolver.resolve(context, rawTimeRange).toString();
        
        World world = player.getWorld();
        long time = world.getTime();
        
        return switch (timeRange.toUpperCase()) {
            case "DAY" -> time >= 0 && time < 12000;
            case "NIGHT" -> time >= 12000 && time < 24000;
            case "SUNRISE" -> time >= 23000 || time < 1000;
            case "SUNSET" -> time >= 11000 && time < 13000;
            case "MORNING" -> time >= 0 && time < 6000;
            case "AFTERNOON" -> time >= 6000 && time < 12000;
            case "EVENING" -> time >= 12000 && time < 18000;
            case "MIDNIGHT" -> time >= 18000 && time < 24000;
            default -> false;
        };
    }
} 