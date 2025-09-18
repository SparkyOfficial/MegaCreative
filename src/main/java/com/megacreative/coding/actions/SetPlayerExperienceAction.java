package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to set a player's experience level and progress
 * 
 * Parameters:
 * - "level": The experience level to set (integer)
 * - "progress": The progress toward next level (0.0-1.0, default: 0.0)
 * - "mode": How to apply the experience ("set", "add", "remove", default: "set")
 */
public class SetPlayerExperienceAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return ExecutionResult.error("No player available for experience modification");
        }
        
        try {
            // Get level parameter
            DataValue levelValue = block.getParameter("level");
            if (levelValue == null || levelValue.isEmpty()) {
                return ExecutionResult.error("Level parameter is required for experience modification");
            }
            
            int level = levelValue.asNumber().intValue();
            
            // Get progress parameter (default to 0.0)
            DataValue progressValue = block.getParameter("progress");
            float progress = 0.0f;
            if (progressValue != null && !progressValue.isEmpty()) {
                progress = progressValue.asNumber().floatValue();
                // Clamp progress between 0.0 and 1.0
                progress = Math.max(0.0f, Math.min(1.0f, progress));
            }
            
            // Get mode parameter (default to "set")
            DataValue modeValue = block.getParameter("mode");
            String mode = "set";
            if (modeValue != null && !modeValue.isEmpty()) {
                mode = modeValue.asString().toLowerCase();
            }
            
            // Apply experience based on mode
            switch (mode) {
                case "set":
                    player.setLevel(level);
                    player.setExp(progress);
                    break;
                case "add":
                    player.giveExpLevels(level);
                    // Progress is not directly modifiable in add mode
                    break;
                case "remove":
                    player.giveExpLevels(-level);
                    // Progress is not directly modifiable in remove mode
                    break;
                default:
                    return ExecutionResult.error("Invalid mode: " + mode + ". Use 'set', 'add', or 'remove'");
            }
            
            player.sendMessage("§aExperience " + mode + " to level " + level + 
                             (mode.equals("set") ? " with " + (progress * 100) + "% progress" : ""));
            
            return ExecutionResult.success("Player experience " + mode + " successfully");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error during experience modification: " + e.getMessage());
        }
    }
}