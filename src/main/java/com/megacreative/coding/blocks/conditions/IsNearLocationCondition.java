package com.megacreative.coding.blocks.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class IsNearLocationCondition implements BlockCondition {
    
    private final Argument<TextValue> xArgument;
    private final Argument<TextValue> yArgument;
    private final Argument<TextValue> zArgument;
    private final Argument<TextValue> distanceArgument;
    
    public IsNearLocationCondition() {
        this.xArgument = new ParameterArgument("x");
        this.yArgument = new ParameterArgument("y");
        this.zArgument = new ParameterArgument("z");
        this.distanceArgument = new ParameterArgument("distance");
    }
    
    @Override
    public boolean evaluate(ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        // Получаем параметры
        TextValue xValue = xArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue yValue = yArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue zValue = zArgument.parse(context.getCurrentBlock()).orElse(null);
        TextValue distanceValue = distanceArgument.parse(context.getCurrentBlock()).orElse(null);
        
        if (xValue == null || yValue == null || zValue == null) {
            return false;
        }
        
        try {
            double x = Double.parseDouble(xValue.get(context));
            double y = Double.parseDouble(yValue.get(context));
            double z = Double.parseDouble(zValue.get(context));
            double distance = distanceValue != null ? Double.parseDouble(distanceValue.get(context)) : 5.0;
            
            Location targetLocation = new Location(player.getWorld(), x, y, z);
            double actualDistance = player.getLocation().distance(targetLocation);
            
            return actualDistance <= distance;
            
        } catch (Exception e) {
            player.sendMessage("§c✗ Ошибка в условии: " + e.getMessage());
            return false;
        }
    }
} 