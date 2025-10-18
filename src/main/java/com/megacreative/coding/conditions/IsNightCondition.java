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
        if (player == null) {
            return false;
        }

        try {
            
            
            
            
            
            long worldTime = player.getWorld().getTime();
            
            return worldTime >= 12542 && worldTime <= 23459;
        } catch (Exception e) {
            
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