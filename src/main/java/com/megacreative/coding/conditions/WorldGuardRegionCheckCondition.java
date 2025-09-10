package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.World;

public class WorldGuardRegionCheckCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            // Get parameters from the block
            DataValue regionNameValue = block.getParameter("region_name");
            DataValue worldValue = block.getParameter("world");
            
            if (regionNameValue == null || regionNameValue.isEmpty()) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: 'region_name' parameter is missing.");
                return false;
            }
            
            String regionName = regionNameValue.asString();
            String worldName = (worldValue != null && !worldValue.isEmpty()) ? 
                worldValue.asString() : player.getWorld().getName();
            
            // Check if WorldGuard is available
            if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: WorldGuard is not installed or enabled.");
                return false;
            }
            
            // Get the world
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: World '" + worldName + "' not found.");
                return false;
            }
            
            // For now, we'll return false as we don't have direct access to WorldGuard API
            // In a real implementation, you would check if the player is in the specified region
            context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: WorldGuard integration not fully implemented.");
            return false;

        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating WorldGuardRegionCheckCondition: " + e.getMessage());
            return false;
        }
    }
}