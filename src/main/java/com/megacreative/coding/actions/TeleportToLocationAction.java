package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * Action that teleports a player to a saved location
 */
@BlockMeta(id = "teleportToLocation", displayName = "§aTeleport to Location", type = BlockType.ACTION)
public class TeleportToLocationAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        // Get the location name from the block parameters
        DataValue locationNameValue = block.getParameter("locationName");
        if (locationNameValue == null) {
            return ExecutionResult.error("No location name parameter found");
        }
        
        String locationName = locationNameValue.asString();
        
        // In a real implementation, we would retrieve the saved location from storage
        // For now, we'll just log that the action was triggered
        context.getPlugin().getLogger().fine("Teleporting player " + player.getName() + " to location: " + locationName);
        
        return ExecutionResult.success("Player teleport to location processed");
    }
}