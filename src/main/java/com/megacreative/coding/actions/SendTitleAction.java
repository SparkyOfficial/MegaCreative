package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for sending a title to a player.
 * This action retrieves title parameters from the container configuration and sends the title.
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
            
            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            
            DataValue titleValue = DataValue.of(params.titleStr);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            DataValue subtitleValue = DataValue.of(params.subtitleStr);
            DataValue resolvedSubtitle = resolver.resolve(context, subtitleValue);
            
            int fadeIn = params.fadeIn;
            int stay = params.stay;
            int fadeOut = params.fadeOut;

            // Send the title to the player
            player.sendTitle(
                resolvedTitle.asString(), 
                resolvedSubtitle.asString(), 
                fadeIn, 
                stay, 
                fadeOut
            );
            
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
                        params.titleStr = getTitleFromItem(titleItem);
                    }
                }
                
                // Get subtitle from the subtitle slot
                Integer subtitleSlot = slotResolver.apply("subtitle");
                if (subtitleSlot != null) {
                    ItemStack subtitleItem = block.getConfigItem(subtitleSlot);
                    if (subtitleItem != null && subtitleItem.hasItemMeta()) {
                        // Extract subtitle from item
                        params.subtitleStr = getSubtitleFromItem(subtitleItem);
                    }
                }
                
                // Get fade in time from the fadein slot
                Integer fadeInSlot = slotResolver.apply("fadein");
                if (fadeInSlot != null) {
                    ItemStack fadeInItem = block.getConfigItem(fadeInSlot);
                    if (fadeInItem != null && fadeInItem.hasItemMeta()) {
                        // Extract fade in time from item
                        params.fadeIn = getFadeInFromItem(fadeInItem, 10);
                    }
                }
                
                // Get stay time from the stay slot
                Integer staySlot = slotResolver.apply("stay");
                if (staySlot != null) {
                    ItemStack stayItem = block.getConfigItem(staySlot);
                    if (stayItem != null && stayItem.hasItemMeta()) {
                        // Extract stay time from item
                        params.stay = getStayFromItem(stayItem, 70);
                    }
                }
                
                // Get fade out time from the fadeout slot
                Integer fadeOutSlot = slotResolver.apply("fadeout");
                if (fadeOutSlot != null) {
                    ItemStack fadeOutItem = block.getConfigItem(fadeOutSlot);
                    if (fadeOutItem != null && fadeOutItem.hasItemMeta()) {
                        // Extract fade out time from item
                        params.fadeOut = getFadeOutFromItem(fadeOutItem, 20);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting title parameters from container in SendTitleAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts title from an item
     */
    private String getTitleFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the title
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Extracts subtitle from an item
     */
    private String getSubtitleFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the subtitle
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Extracts fade in time from an item
     */
    private int getFadeInFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse fade in time from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts stay time from an item
     */
    private int getStayFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse stay time from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Extracts fade out time from an item
     */
    private int getFadeOutFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse fade out time from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Integer.parseInt(cleanName);
                }
            }
            
            // Fallback to item amount
            return item.getAmount();
        } catch (Exception e) {
            return defaultValue;
        }
    }
    
    /**
     * Helper class to hold title parameters
     */
    private static class SendTitleParams {
        String titleStr = "";
        String subtitleStr = "";
        int fadeIn = 10;
        int stay = 70;
        int fadeOut = 20;
    }
}