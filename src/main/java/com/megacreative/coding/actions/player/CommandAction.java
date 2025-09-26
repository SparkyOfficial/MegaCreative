package com.megacreative.coding.actions.player;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for executing a command.
 * This action retrieves a command from the container configuration and executes it.
 */
@BlockMeta(id = "command", displayName = "§aExecute Command", type = BlockType.ACTION)
public class CommandAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the command from the container configuration
            String command = getCommandFromContainer(block, context);
            if (command == null || command.isEmpty()) {
                return ExecutionResult.error("Command is not configured");
            }

            // Resolve any placeholders in the command
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue commandValue = DataValue.of(command);
            DataValue resolvedCommand = resolver.resolve(context, commandValue);
            
            // Execute the command
            boolean success = Bukkit.dispatchCommand(player, resolvedCommand.asString());
            if (success) {
                return ExecutionResult.success("Command executed successfully");
            } else {
                return ExecutionResult.error("Failed to execute command: " + resolvedCommand.asString());
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute command: " + e.getMessage());
        }
    }
    
    /**
     * Gets command from the container configuration
     */
    private String getCommandFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get command from the command slot
                Integer commandSlot = slotResolver.apply("command_slot");
                if (commandSlot != null) {
                    ItemStack commandItem = block.getConfigItem(commandSlot);
                    if (commandItem != null && commandItem.hasItemMeta()) {
                        // Extract command from item
                        return getCommandFromItem(commandItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting command from container in CommandAction: " + e.getMessage());
        }
        
        return null;
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
}