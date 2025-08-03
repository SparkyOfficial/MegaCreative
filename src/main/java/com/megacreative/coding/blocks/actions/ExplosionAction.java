package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class ExplosionAction implements BlockAction {
    
    private final Argument<TextValue> powerArgument;
    private final Argument<TextValue> setFireArgument;
    
    public ExplosionAction() {
        this.powerArgument = new ParameterArgument("power");
        this.setFireArgument = new ParameterArgument("setFire");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        try {
            String powerStr = powerArgument.parse(context.getCurrentBlock()).get().get(context);
            String setFireStr = setFireArgument.parse(context.getCurrentBlock()).get().get(context);
            
            float power = Float.parseFloat(powerStr);
            boolean setFire = Boolean.parseBoolean(setFireStr);
            
            Player player = context.getPlayer();
            Location explosionLocation = player.getLocation();
            
            explosionLocation.getWorld().createExplosion(explosionLocation, power, setFire);
            
            context.getPlugin().getLogger().info("Created explosion with power " + power + " for player " + player.getName());
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Failed to create explosion: " + e.getMessage());
        }
    }
} 