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
 * Action for sending a message to a player.
 * This action retrieves a message from the container configuration and sends it to the player.
 */
public class SendMessageAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            context.getPlugin().getLogger().warning("No player found in execution context for SendMessageAction");
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            context.getPlugin().getLogger().info("Executing SendMessageAction for player: " + player.getName());
            
            // Get the message from the container configuration
            String message = getMessageFromContainer(block, context);
            context.getPlugin().getLogger().info("Retrieved message from container: " + message);
            
            if (message == null || message.isEmpty()) {
                context.getPlugin().getLogger().warning("Message is not configured for SendMessageAction");
                return ExecutionResult.error("Message is not configured");
            }

            // Resolve any placeholders in the message
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedMessage = resolver.resolveString(context, message);
            context.getPlugin().getLogger().info("Resolved message: " + resolvedMessage);
            
            // Send the message to the player
            player.sendMessage(resolvedMessage);
            context.getPlugin().getLogger().info("Successfully sent message to player: " + player.getName());
            return ExecutionResult.success("Message sent successfully");
        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Failed to send message: " + e.getMessage());
            return ExecutionResult.error("Failed to send message: " + e.getMessage());
        }
    }
    
    /**
     * Gets message from the container configuration
     */
    private String getMessageFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            context.getPlugin().getLogger().info("Getting message from container for block with action: " + block.getAction());
            
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                context.getPlugin().getLogger().info("Slot resolver found for action: " + block.getAction());
                
                // Get message from the message slot
                Integer messageSlot = slotResolver.apply("message_slot");
                context.getPlugin().getLogger().info("Message slot index: " + messageSlot);
                
                if (messageSlot != null) {
                    ItemStack messageItem = block.getConfigItem(messageSlot);
                    context.getPlugin().getLogger().info("Message item from slot: " + (messageItem != null ? messageItem.getType().name() : "null"));
                    
                    if (messageItem != null && messageItem.hasItemMeta()) {
                        // Extract message from item name or lore
                        String message = getMessageFromItem(messageItem);
                        context.getPlugin().getLogger().info("Extracted message from item: " + message);
                        return message;
                    }
                }
            }
            
            // 🎆 ENHANCED: Fallback to parameter-based configuration
            DataValue messageParam = block.getParameter("message");
            context.getPlugin().getLogger().info("Message parameter value: " + (messageParam != null ? messageParam.asString() : "null"));
            
            if (messageParam != null && !messageParam.isEmpty()) {
                return messageParam.asString();
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting message from container in SendMessageAction: " + e.getMessage());
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