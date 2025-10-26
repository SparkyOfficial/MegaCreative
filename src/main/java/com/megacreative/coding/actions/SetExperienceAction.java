package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action that sets a player's experience
 */
@BlockMeta(id = "setExperience", displayName = "§aSet Experience", type = BlockType.ACTION)
public class SetExperienceAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        // Get the experience value from the block parameters
        DataValue experienceValue = block.getParameter("experience");
        if (experienceValue == null) {
            return ExecutionResult.error("No experience parameter found");
        }
        
        try {
            int experience = Integer.parseInt(experienceValue.asString());
            
            // Set the player's experience
            player.setTotalExperience(experience);
            
            context.getPlugin().getLogger().fine("Set experience to " + experience + " for player " + player.getName());
            return ExecutionResult.success("Experience set successfully");
        } catch (NumberFormatException e) {
            return ExecutionResult.error("Invalid experience value: " + experienceValue.asString());
        }
    }
}