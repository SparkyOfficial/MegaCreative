package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for sending a title to a player.
 * This action sends a title and subtitle to the player with configurable timing from container configuration.
 */
public class SendTitleAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get title parameters from the container configuration
            SendTitleParams params = getTitleParamsFromContainer(block, context);
            
            if (params.title == null || params.title.isEmpty()) {
                return ExecutionResult.error("Title is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTitle = resolver.resolveString(context, params.title);
            String resolvedSubtitle = resolver.resolveString(context, params.subtitle);

            // Send the title to the player
            player.sendTitle(resolvedTitle, resolvedSubtitle, params.fadeIn, params.stay, params.fadeOut);
            
            return ExecutionResult.success("Title sent successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to send title: " + e.getMessage());
        }
    }
    
    /**
     * Gets title parameters from the container configuration
     */
    private SendTitleParams getTitleParamsFromContainer(CodeBlock block, ExecutionContext context) {
        SendTitleParams params = new SendTitleParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get title from the title slot
                Integer titleSlot = slotResolver.apply("title");
                if (titleSlot != null) {
                    ItemStack titleItem = block.getConfigItem(titleSlot);
                    if (titleItem != null && titleItem.hasItemMeta()) {
                        // Extract title from item
                        params.title = getTextFromItem(titleItem);
                    }
                }
                
                // Get subtitle from the subtitle slot
                Integer subtitleSlot = slotResolver.apply("subtitle");
                if (subtitleSlot != null) {
                    ItemStack subtitleItem = block.getConfigItem(subtitleSlot);
                    if (subtitleItem != null && subtitleItem.hasItemMeta()) {
                        // Extract subtitle from item
                        params.subtitle = getTextFromItem(subtitleItem);
                    }
                }
                
                // Get fadeIn from the fadeIn slot
                Integer fadeInSlot = slotResolver.apply("fadein");
                if (fadeInSlot != null) {
                    ItemStack fadeInItem = block.getConfigItem(fadeInSlot);
                    if (fadeInItem != null && fadeInItem.hasItemMeta()) {
                        // Extract fadeIn from item
                        params.fadeIn = getNumberFromItem(fadeInItem, 10);
                    }
                }
                
                // Get stay from the stay slot
                Integer staySlot = slotResolver.apply("stay");
                if (staySlot != null) {
                    ItemStack stayItem = block.getConfigItem(staySlot);
                    if (stayItem != null && stayItem.hasItemMeta()) {
                        // Extract stay from item
                        params.stay = getNumberFromItem(stayItem, 70);
                    }
                }
                
                // Get fadeOut from the fadeout slot
                Integer fadeOutSlot = slotResolver.apply("fadeout");
                if (fadeOutSlot != null) {
                    ItemStack fadeOutItem = block.getConfigItem(fadeOutSlot);
                    if (fadeOutItem != null && fadeOutItem.hasItemMeta()) {
                        // Extract fadeOut from item
                        params.fadeOut = getNumberFromItem(fadeOutItem, 20);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting title parameters from container in SendTitleAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts text from an item
     */
    private String getTextFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the text
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Extracts number from an item
     */
    private int getNumberFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse number from display name (e.g., "fadein:10")
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    if (cleanName.contains(":")) {
                        String[] parts = cleanName.split(":");
                        if (parts.length > 1) {
                            return Math.max(0, Integer.parseInt(parts[1].trim()));
                        }
                    }
                }
            }
            
            // Fallback to item amount
            return Math.max(0, item.getAmount());
        } catch (Exception e) {
            return Math.max(0, defaultValue);
        }
    }
    
    /**
     * Helper class to hold title parameters
     */
    private static class SendTitleParams {
        String title = "";
        String subtitle = "";
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;
    }
}