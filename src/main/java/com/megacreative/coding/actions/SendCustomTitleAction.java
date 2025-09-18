package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to send a custom title and subtitle to a player with configurable timing
 * 
 * Parameters:
 * - "title": The main title text (required)
 * - "subtitle": The subtitle text (optional)
 * - "fadeIn": Fade in time in ticks (default: 10)
 * - "stay": Stay time in ticks (default: 70)
 * - "fadeOut": Fade out time in ticks (default: 20)
 */
public class SendCustomTitleAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        
        if (player == null) {
            return ExecutionResult.error("No player available for title display");
        }
        
        try {
            // Get title parameter (required)
            DataValue titleValue = block.getParameter("title");
            if (titleValue == null || titleValue.isEmpty()) {
                return ExecutionResult.error("Title parameter is required for custom title display");
            }
            
            String title = titleValue.asString();
            
            // Get subtitle parameter (optional)
            DataValue subtitleValue = block.getParameter("subtitle");
            String subtitle = "";
            if (subtitleValue != null && !subtitleValue.isEmpty()) {
                subtitle = subtitleValue.asString();
            }
            
            // Get timing parameters with defaults
            DataValue fadeInValue = block.getParameter("fadeIn");
            int fadeIn = 10; // Default 0.5 seconds
            if (fadeInValue != null && !fadeInValue.isEmpty()) {
                fadeIn = Math.max(0, fadeInValue.asNumber().intValue());
            }
            
            DataValue stayValue = block.getParameter("stay");
            int stay = 70; // Default 3.5 seconds
            if (stayValue != null && !stayValue.isEmpty()) {
                stay = Math.max(0, stayValue.asNumber().intValue());
            }
            
            DataValue fadeOutValue = block.getParameter("fadeOut");
            int fadeOut = 20; // Default 1 second
            if (fadeOutValue != null && !fadeOutValue.isEmpty()) {
                fadeOut = Math.max(0, fadeOutValue.asNumber().intValue());
            }
            
            // Send the title to the player
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            
            // Send confirmation message to player (only in debug mode)
            if (context.isDebugMode()) {
                player.sendMessage("§aTitle sent: " + title + 
                                 (subtitle.isEmpty() ? "" : " / " + subtitle));
            }
            
            return ExecutionResult.success("Custom title sent successfully");
            
        } catch (Exception e) {
            return ExecutionResult.error("Error during title display: " + e.getMessage());
        }
    }
}