package com.megacreative.coding.blocks.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.arguments.ParameterArgument;
import com.megacreative.coding.arguments.Argument;
import com.megacreative.coding.values.TextValue;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

public class SpawnMobAction implements BlockAction {
    
    private final Argument<TextValue> mobTypeArgument;
    private final Argument<TextValue> countArgument;
    
    public SpawnMobAction() {
        this.mobTypeArgument = new ParameterArgument("mobType");
        this.countArgument = new ParameterArgument("count");
    }
    
    @Override
    public void execute(ExecutionContext context) {
        try {
            String mobTypeStr = mobTypeArgument.parse(context.getCurrentBlock()).get().get(context);
            String countStr = countArgument.parse(context.getCurrentBlock()).get().get(context);
            
            EntityType mobType = EntityType.valueOf(mobTypeStr.toUpperCase());
            int count = Integer.parseInt(countStr);
            
            Player player = context.getPlayer();
            Location spawnLocation = player.getLocation();
            
            for (int i = 0; i < count; i++) {
                spawnLocation.getWorld().spawnEntity(spawnLocation, mobType);
            }
            
            context.getPlugin().getLogger().info("Spawned " + count + " " + mobType.name() + " for player " + player.getName());
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Failed to spawn mob: " + e.getMessage());
        }
    }
} 