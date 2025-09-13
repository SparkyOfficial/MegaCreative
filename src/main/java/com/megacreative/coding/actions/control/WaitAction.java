package com.megacreative.coding.actions.control;

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
 * 🎆 ENHANCED: Action for adding delays in script execution
 * Supports both container-based configuration and parameter-based configuration
 */
public class WaitAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get delay parameters from configuration
            int delayTicks = getDelayFromContainer(block, context);
            
            if (delayTicks <= 0) {
                return ExecutionResult.error("Invalid delay value");
            }

            // Validate and constrain delay (max 20 minutes = 24000 ticks)
            delayTicks = Math.max(1, Math.min(24000, delayTicks));
            
            // Schedule the continuation of script execution after delay
            // Note: In a real implementation, this would need to integrate with
            // the script execution engine to properly pause and resume execution
            final int finalDelayTicks = delayTicks;
            final Player finalPlayer = player;
            context.getPlugin().getServer().getScheduler().runTaskLater(
                context.getPlugin(), 
                () -> {
                    // This is where the script would continue execution
                    finalPlayer.sendMessage("§a⏰ Wait completed (" + (finalDelayTicks / 20.0) + " seconds)");
                }, 
                delayTicks
            );
            
            double seconds = delayTicks / 20.0;
            return ExecutionResult.success("Waiting " + seconds + " seconds...");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to execute wait: " + e.getMessage());
        }
    }
    
    /**
     * 🎆 ENHANCED: Gets delay from container configuration with fallbacks
     */
    private int getDelayFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get delay from the delay slot
                Integer delaySlot = slotResolver.apply("delay");
                if (delaySlot != null) {
                    ItemStack delayItem = block.getConfigItem(delaySlot);
                    if (delayItem != null && delayItem.hasItemMeta()) {
                        int ticks = getTicksFromItem(delayItem);
                        if (ticks > 0) {
                            return ticks;
                        }
                    }
                }
            }
            
            // 🎆 ENHANCED: Fallback to parameter-based configuration
            DataValue delayParam = block.getParameter("delay");
            DataValue secondsParam = block.getParameter("seconds");
            DataValue ticksParam = block.getParameter("ticks");
            
            if (delayParam != null && !delayParam.isEmpty()) {
                try {
                    return Integer.parseInt(delayParam.asString());
                } catch (NumberFormatException e) {
                    // Try next fallback
                }
            }
            
            if (secondsParam != null && !secondsParam.isEmpty()) {
                try {
                    double seconds = Double.parseDouble(secondsParam.asString());
                    return (int) (seconds * 20); // Convert seconds to ticks
                } catch (NumberFormatException e) {
                    // Try next fallback
                }
            }
            
            if (ticksParam != null && !ticksParam.isEmpty()) {
                try {
                    return Integer.parseInt(ticksParam.asString());
                } catch (NumberFormatException e) {
                    // Use default
                }
            }
            
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting delay from container: " + e.getMessage());
        }
        
        return 20; // Default: 1 second
    }
    
    /**
     * 🎆 ENHANCED: Extracts delay in ticks from an item with smart parsing
     */
    private int getTicksFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                String cleanName = org.bukkit.ChatColor.stripColor(displayName).trim();
                
                try {
                    // Parse different formats
                    if (cleanName.endsWith("s") || cleanName.endsWith("sec") || cleanName.endsWith("seconds")) {
                        // Parse as seconds
                        String numberPart = cleanName.replaceAll("[^0-9.]", "");
                        double seconds = Double.parseDouble(numberPart);
                        return (int) (seconds * 20); // Convert to ticks
                    } else if (cleanName.endsWith("t") || cleanName.endsWith("tick") || cleanName.endsWith("ticks")) {
                        // Parse as ticks
                        String numberPart = cleanName.replaceAll("[^0-9]", "");
                        return Integer.parseInt(numberPart);
                    } else {
                        // Try parsing as raw number (assume ticks)
                        return Integer.parseInt(cleanName);
                    }
                } catch (NumberFormatException e) {
                    // Check lore for delay information
                    if (meta.hasLore()) {
                        for (String line : meta.getLore()) {
                            String cleanLine = org.bukkit.ChatColor.stripColor(line).trim();
                            if (cleanLine.startsWith("Delay:") || cleanLine.startsWith("Wait:")) {
                                String delayPart = cleanLine.substring(cleanLine.indexOf(":") + 1).trim();
                                try {
                                    if (delayPart.endsWith("s")) {
                                        double seconds = Double.parseDouble(delayPart.replace("s", ""));
                                        return (int) (seconds * 20);
                                    } else {
                                        return Integer.parseInt(delayPart);
                                    }
                                } catch (NumberFormatException e2) {
                                    // Continue to next lore line
                                }
                            }
                        }
                    }
                }
            }
        }
        return 20; // Default: 1 second
    }
}