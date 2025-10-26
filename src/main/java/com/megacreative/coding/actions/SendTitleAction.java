package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action to send a title to a player
 * 
 * @author Андрій Будильников
 */
@BlockMeta(id = "sendTitle", displayName = "§bSend Title", type = BlockType.ACTION)
public class SendTitleAction implements BlockAction {
    
    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player in execution context");
        }
        
        try {
            // Get parameters
            DataValue titleValue = block.getParameter("title");
            DataValue subtitleValue = block.getParameter("subtitle");
            DataValue fadeInValue = block.getParameter("fadein");
            DataValue stayValue = block.getParameter("stay");
            DataValue fadeOutValue = block.getParameter("fadeout");
            
            if (titleValue == null) {
                return ExecutionResult.error("Missing required parameter: title");
            }
            
            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            String title = resolvedTitle.asString();
            String subtitle = "";
            
            if (subtitleValue != null) {
                DataValue resolvedSubtitle = resolver.resolve(context, subtitleValue);
                subtitle = resolvedSubtitle.asString();
            }
            
            // Get timing values (default values)
            int fadeIn = 10;
            int stay = 70;
            int fadeOut = 20;
            
            if (fadeInValue != null) {
                DataValue resolvedFadeIn = resolver.resolve(context, fadeInValue);
                fadeIn = Math.max(0, resolvedFadeIn.asNumber().intValue());
            }
            
            if (stayValue != null) {
                DataValue resolvedStay = resolver.resolve(context, stayValue);
                stay = Math.max(0, resolvedStay.asNumber().intValue());
            }
            
            if (fadeOutValue != null) {
                DataValue resolvedFadeOut = resolver.resolve(context, fadeOutValue);
                fadeOut = Math.max(0, resolvedFadeOut.asNumber().intValue());
            }
            
            // Send title
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            
            return ExecutionResult.success("Sent title to player");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send title: " + e.getMessage());
        }
    }
}