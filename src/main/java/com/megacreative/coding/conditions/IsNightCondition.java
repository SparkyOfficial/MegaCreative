package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;

/**
 * Condition for checking if it's night time in the player's world from the new parameter system.
 * This condition returns true if it's night time in the player's world.
 */
@BlockMeta(id = "isNight", displayName = "§aIs Night", type = BlockType.CONDITION)
public class IsNightCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null || player.getWorld() == null) {
            return false;
        }

        try {
            // Get parameters from the new parameter system
            // This condition doesn't require any parameters, but we'll check if a time parameter is provided
            // for backward compatibility or future expansion
            // For now, we'll just check if it's currently night in the player's world
            
            long worldTime = player.getWorld().getTime();
            // Night time in Minecraft is from 12542 to 23459 ticks
            return worldTime >= 12542 && worldTime <= 23459;
        } catch (Exception e) {
            // If there's an error, return false
            context.getPlugin().getLogger().warning("Error in IsNightCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Helper class to hold time parameters
     */
    private static class IsNightParams {
        String timeStr = "";
    }
}