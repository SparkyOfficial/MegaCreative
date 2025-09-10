package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;

/**
 * Action for sending a title to a player.
 * This action sends a title and subtitle to the player with configurable timing.
 */
public class SendTitleAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the title parameter from the block
            DataValue titleValue = block.getParameter("title");
            if (titleValue == null) {
                return ExecutionResult.error("Title parameter is missing");
            }

            // Get the subtitle parameter from the block (optional)
            DataValue subtitleValue = block.getParameter("subtitle");

            // Get timing parameters (with defaults)
            int fadeIn = 10;
            DataValue fadeInValue = block.getParameter("fadeIn");
            if (fadeInValue != null) {
                try {
                    fadeIn = Math.max(0, fadeInValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default fadeIn if parsing fails
                }
            }

            int stay = 70;
            DataValue stayValue = block.getParameter("stay");
            if (stayValue != null) {
                try {
                    stay = Math.max(0, stayValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default stay if parsing fails
                }
            }

            int fadeOut = 20;
            DataValue fadeOutValue = block.getParameter("fadeOut");
            if (fadeOutValue != null) {
                try {
                    fadeOut = Math.max(0, fadeOutValue.asNumber().intValue());
                } catch (NumberFormatException e) {
                    // Use default fadeOut if parsing fails
                }
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            String title = resolvedTitle.asString();
            if (title == null || title.isEmpty()) {
                return ExecutionResult.error("Title is empty or null");
            }

            String subtitle = "";
            if (subtitleValue != null) {
                DataValue resolvedSubtitle = resolver.resolve(context, subtitleValue);
                subtitle = resolvedSubtitle.asString();
            }

            // Send the title to the player
            player.sendTitle(title, subtitle, fadeIn, stay, fadeOut);
            
            return ExecutionResult.success("Title sent successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send title: " + e.getMessage());
        }
    }
}