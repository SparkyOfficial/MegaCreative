package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class SetTimeAction implements BlockAction {
    
    private final Argument<TextValue> timeArgument;
    
    public SetTimeAction() {
        this.timeArgument = new ParameterArgument("time");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        try {
            String timeStr = timeArgument.parse(context.getCurrentBlock()).get().get(context);
            long time = Long.parseLong(timeStr);
            
            Player player = context.getPlayer();
            World world = player.getWorld();
            world.setTime(time);
            
            context.getPlugin().getLogger().info("Set time to " + time + " in world " + world.getName());
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Failed to set time: " + e.getMessage());
        }
    }
} 