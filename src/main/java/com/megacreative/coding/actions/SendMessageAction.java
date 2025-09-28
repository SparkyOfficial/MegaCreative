package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.services.MessagingService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for sending a message to a player.
 * This action retrieves a message from the container configuration and sends it to the player.
 */
public class SendMessageAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("");
        }

        try {
            // Get the message from the container configuration
            String message = getMessageFromContainer(block, context);
            
            if (message == null || message.isEmpty()) {
                return ExecutionResult.error("");
            }

            // Resolve any placeholders in the message
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedMessage = resolver.resolveString(context, message);
            
            // Send the message to the player using Adventure API if available
            MessagingService messagingService = context.getPlugin().getServiceRegistry().getMessagingService();
            if (messagingService != null) {
                messagingService.sendMessage(player, resolvedMessage);
            } else {
                // Fallback to regular sendMessage if Adventure API is not available
                player.sendMessage(resolvedMessage);
            }
            
            return ExecutionResult.success("");
        } catch (Exception e) {
            return ExecutionResult.error("");
        }
    }
    
    /**
     * Gets message from the container configuration
     */
    private String getMessageFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get message from the message slot
                Integer messageSlot = slotResolver.apply("message_slot");
                
                if (messageSlot != null) {
                    ItemStack messageItem = block.getConfigItem(messageSlot);
                    
                    if (messageItem != null && messageItem.hasItemMeta()) {
                        // Extract message from item name or lore
                        return getMessageFromItem(messageItem);
                    }
                }
            }
            
            // Fallback to parameter-based configuration
            DataValue messageParam = block.getParameter("message");
            
            if (messageParam != null && !messageParam.isEmpty()) {
                return messageParam.asString();
            }
            
        } catch (Exception e) {
            // Log the exception instead of ignoring it
            context.getPlugin().getLogger().warning("Error getting message from container: " + e.getMessage());
        }
        return null;

    }
    
    /**
     * Extracts message from an item
     */
    private String getMessageFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Return the display name as is, preserving color codes
                return displayName;
            }
        }
        return null;
    }
}