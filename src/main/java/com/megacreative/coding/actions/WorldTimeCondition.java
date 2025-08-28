package com.megacreative.coding.actions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.values.DataValue;
import com.megacreative.coding.variables.VariableManager;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class WorldTimeCondition implements BlockCondition {
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        CodeBlock block = context.getCurrentBlock();
        
        if (player == null || block == null) return false;
        
        VariableManager variableManager = context.getPlugin().getVariableManager();
        if (variableManager == null) return false;
        
        ParameterResolver resolver = new ParameterResolver(variableManager);
        
        DataValue rawTimeRange = block.getParameter("timeRange");
        if (rawTimeRange == null) return false;
        
        String timeRange = resolver.resolve(context, rawTimeRange).asString();
        
        if (timeRange == null) return false;
        
        World world = player.getWorld();
        long time = world.getTime();
        
        switch (timeRange.toUpperCase()) {
            case "DAY":
                return time >= 0 && time < 12000;
            case "NIGHT":
                return time >= 12000 && time < 24000;
            case "SUNRISE":
                return time >= 23000 || time < 1000;
            case "SUNSET":
                return time >= 11000 && time < 13000;
            case "MORNING":
                return time >= 0 && time < 6000;
            case "AFTERNOON":
                return time >= 6000 && time < 12000;
            case "EVENING":
                return time >= 12000 && time < 18000;
            case "MIDNIGHT":
                return time >= 18000 && time < 24000;
            default:
                return false;
        }
    }
} 