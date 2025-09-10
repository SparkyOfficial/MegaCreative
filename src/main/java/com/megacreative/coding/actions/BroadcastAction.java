package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for broadcasting a message to all players.
 * This action retrieves a message from the container configuration and broadcasts it.
 */
public class BroadcastAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        try {
            // Get the message from the container configuration
            String message = getMessageFromContainer(block, context);
            if (message == null || message.isEmpty()) {
                return ExecutionResult.error("Message is not configured");
            }

            // Resolve any placeholders in the message
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedMessage = resolver.resolveString(context, message);
            
            // Broadcast the message
            Bukkit.broadcastMessage(resolvedMessage);
            return ExecutionResult.success("Message broadcasted successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to broadcast message: " + e.getMessage());
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
                        // Extract message from item
                        return getMessageFromItem(messageItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting message from container in BroadcastAction: " + e.getMessage());
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
                // Remove color codes and return the message
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}