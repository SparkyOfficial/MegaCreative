package com.megacreative.coding.actions.scoreboard;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.coding.values.DataValue;
import com.megacreative.services.BlockConfigService;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.function.Function;

/**
 * Action for incrementing a score on a scoreboard.
 * This action retrieves parameters from the container configuration and increments a score.
 */
@BlockMeta(id = "incrementScore", displayName = "§aIncrement Score", type = BlockType.ACTION)
public class IncrementScoreAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            IncrementScoreParams params = getScoreParamsFromContainer(block, context);

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue keyVal = DataValue.of(params.keyStr);
            DataValue resolvedKey = resolver.resolve(context, keyVal);
            
            DataValue incrementVal = DataValue.of(params.incrementStr);
            DataValue resolvedIncrement = resolver.resolve(context, incrementVal);
            
            // Parse parameters
            String key = resolvedKey.asString();
            String incrementStr = resolvedIncrement.asString();
            
            if (key == null || key.isEmpty()) {
                return ExecutionResult.error("Invalid score key");
            }

            // Parse the increment value as a number
            int increment;
            try {
                increment = Integer.parseInt(incrementStr);
            } catch (NumberFormatException e) {
                return ExecutionResult.error("Invalid increment value: " + incrementStr);
            }

            // Increment the score using the Bukkit scoreboard system
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                return ExecutionResult.error("No scoreboard found for player");
            }
            
            Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                return ExecutionResult.error("No main objective found on scoreboard");
            }
            
            Score score = objective.getScore(key);
            score.setScore(score.getScore() + increment);

            return ExecutionResult.success("Score incremented successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to increment score: " + e.getMessage());
        }
    }
    
    /**
     * Gets score parameters from the container configuration
     */
    private IncrementScoreParams getScoreParamsFromContainer(CodeBlock block, ExecutionContext context) {
        IncrementScoreParams params = new IncrementScoreParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get key from the key slot
                Integer keySlot = slotResolver.apply("key");
                if (keySlot != null) {
                    ItemStack keyItem = block.getConfigItem(keySlot);
                    if (keyItem != null && keyItem.hasItemMeta()) {
                        // Extract key from item
                        params.keyStr = getKeyFromItem(keyItem);
                    }
                }
                
                // Get increment value from the increment slot
                Integer incrementSlot = slotResolver.apply("increment");
                if (incrementSlot != null) {
                    ItemStack incrementItem = block.getConfigItem(incrementSlot);
                    if (incrementItem != null && incrementItem.hasItemMeta()) {
                        // Extract increment value from item
                        params.incrementStr = getIncrementFromItem(incrementItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting score parameters from container in IncrementScoreAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts key from an item
     */
    private String getKeyFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the key
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Extracts increment value from an item
     */
    private String getIncrementFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the increment value
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "1";
    }
    
    /**
     * Helper class to hold score parameters
     */
    private static class IncrementScoreParams {
        String keyStr = "";
        String incrementStr = "1";
    }
}