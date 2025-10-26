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
 * Action that sends a custom title to a player with configurable timing
 */
@BlockMeta(id = "sendCustomTitle", displayName = "§aSend Custom Title", type = BlockType.ACTION)
public class SendCustomTitleAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        // Get the title parameters from the block
        DataValue titleValue = block.getParameter("title");
        DataValue subtitleValue = block.getParameter("subtitle");
        DataValue fadeInValue = block.getParameter("fadeIn");
        DataValue stayValue = block.getParameter("stay");
        DataValue fadeOutValue = block.getParameter("fadeOut");
        
        String title = titleValue != null ? titleValue.asString() : "";
        String subtitle = subtitleValue != null ? subtitleValue.asString() : "";
        
        int fadeIn = fadeInValue != null ? parseInt(fadeInValue.asString(), 10) : 10;
        int stay = stayValue != null ? parseInt(stayValue.asString(), 70) : 70;
        int fadeOut = fadeOutValue != null ? parseInt(fadeOutValue.asString(), 20) : 20;
        
        // Send the custom title to the player
        player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
        
        context.getPlugin().getLogger().fine("Sent custom title '" + title + "' with subtitle '" + subtitle + "' to player " + player.getName());
        return ExecutionResult.success("Custom title sent successfully");
    }
    
    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}