package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.ScriptEngine;
import com.megacreative.coding.annotations.BlockMeta; // Added import
import com.megacreative.coding.BlockType; // Added import
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.function.Function;

/**
 * 🎆 ENHANCED: Action for adding delays in script execution
 * Supports both container-based configuration and parameter-based configuration
 */
@BlockMeta(id = "wait", displayName = "§aWait", type = BlockType.ACTION) // Added annotation
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
            
            // Create final variables for use in lambda expressions
            final double seconds = delayTicks / 20.0;
            final CodeBlock nextBlock = block.getNextBlock();
            final Player finalPlayer = player;
            
            if (nextBlock == null) {
                // If there's no next block, just wait and finish
                context.getPlugin().getServer().getScheduler().runTaskLater(
                    context.getPlugin(), 
                    () -> {
                        finalPlayer.sendMessage("§a⏰ Wait completed (" + seconds + " seconds)");
                    }, 
                    delayTicks
                );
                
                return ExecutionResult.success("Waiting " + seconds + " seconds...").withPause();
            } else {
                // Schedule the continuation of script execution after delay
                context.getPlugin().getServer().getScheduler().runTaskLater(
                    context.getPlugin(), 
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            try {
                                // Continue execution with the next block
                                // Get ScriptEngine from plugin's service registry
                                ScriptEngine scriptEngine = context.getPlugin().getServiceRegistry().getService(ScriptEngine.class);
                                if (scriptEngine != null) {
                                    scriptEngine.executeBlockChain(nextBlock, finalPlayer, "wait_completed")
                                        .whenComplete((result, throwable) -> {
                                            if (throwable != null) {
                                                context.getPlugin().getLogger().warning("Error continuing script after wait: " + throwable.getMessage());
                                            } else if (result != null && !result.isSuccess()) {
                                                context.getPlugin().getLogger().warning("Script execution failed after wait: " + result.getMessage());
                                            }
                                        });
                                } else {
                                    context.getPlugin().getLogger().warning("ScriptEngine not available to continue execution after wait");
                                }
                                finalPlayer.sendMessage("§a⏰ Wait completed (" + seconds + " seconds)");
                            } catch (Exception e) {
                                context.getPlugin().getLogger().warning("Error in wait action continuation: " + e.getMessage());
                            }
                        }
                    }, 
                    delayTicks
                );
                
                // Return a result that indicates the current chain should stop
                return ExecutionResult.success("Waiting " + seconds + " seconds...").withPause();
            }
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