package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class ExecuteAsyncCommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("Player not found.");
        }

        try {
            // Get parameters from the container configuration
            AsyncCommandParams params = getCommandParamsFromContainer(block, context);
            
            if (params.command == null || params.command.isEmpty()) {
                return ExecutionResult.error("Command is not configured.");
            }

            // Resolve parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedCommand = resolver.resolveString(context, params.command);

            // Execute command asynchronously with optional delay
            Bukkit.getScheduler().runTaskLaterAsynchronously(context.getPlugin(), () -> {
                try {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), resolvedCommand);
                } catch (Exception e) {
                    context.getPlugin().getLogger().severe("Error executing async command: " + e.getMessage());
                }
            }, params.delay);

            return ExecutionResult.success("Command will be executed asynchronously.");
        } catch (Exception e) {
            return ExecutionResult.error("Error executing async command: " + e.getMessage());
        }
    }
    
    /**
     * Gets command parameters from the container configuration
     */
    private AsyncCommandParams getCommandParamsFromContainer(CodeBlock block, ExecutionContext context) {
        AsyncCommandParams params = new AsyncCommandParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get command from the command slot
                Integer commandSlot = slotResolver.apply("command");
                if (commandSlot != null) {
                    ItemStack commandItem = block.getConfigItem(commandSlot);
                    if (commandItem != null && commandItem.hasItemMeta()) {
                        // Extract command from item
                        params.command = getCommandFromItem(commandItem);
                    }
                }
                
                // Get delay from the delay slot
                Integer delaySlot = slotResolver.apply("delay");
                if (delaySlot != null) {
                    ItemStack delayItem = block.getConfigItem(delaySlot);
                    if (delayItem != null && delayItem.hasItemMeta()) {
                        // Extract delay from item
                        params.delay = getDelayFromItem(delayItem, 0);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting command parameters from container in ExecuteAsyncCommandAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts command from an item
     */
    private String getCommandFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the command
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts delay from an item
     */
    private int getDelayFromItem(ItemStack item, int defaultValue) {
        try {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null && !displayName.isEmpty()) {
                    // Try to parse delay from display name
                    String cleanName = displayName.replaceAll("[§0-9]", "").trim();
                    return Math.max(0, Integer.parseInt(cleanName));
                }
            }
            
            // Fallback to item amount
            return Math.max(0, item.getAmount());
        } catch (Exception e) {
            return Math.max(0, defaultValue);
        }
    }
    
    /**
     * Helper class to hold async command parameters
     */
    private static class AsyncCommandParams {
        String command = "";
        int delay = 0;
    }
}