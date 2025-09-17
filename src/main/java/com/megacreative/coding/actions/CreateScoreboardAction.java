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
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.DisplaySlot;

import java.util.function.Function;

/**
 * Action for creating a scoreboard.
 * This action retrieves parameters from the container configuration and creates a scoreboard.
 */
public class CreateScoreboardAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            String title = getTitleFromContainer(block, context);

            // Resolve any placeholders in the title
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue titleValue = DataValue.of(title);
            DataValue resolvedTitle = resolver.resolve(context, titleValue);
            
            // Parse parameters
            String scoreboardTitle = resolvedTitle.asString();
            
            if (scoreboardTitle == null || scoreboardTitle.isEmpty()) {
                return ExecutionResult.error("Invalid scoreboard title");
            }

            // Create the scoreboard using the Bukkit scoreboard system
            Scoreboard scoreboard = player.getScoreboard();
            if (scoreboard == null) {
                scoreboard = org.bukkit.Bukkit.getScoreboardManager().getNewScoreboard();
            }
            
            // Create or get the objective
            Objective objective = scoreboard.getObjective("main");
            if (objective == null) {
                objective = scoreboard.registerNewObjective("main", "dummy", scoreboardTitle);
            } else {
                objective.setDisplayName(scoreboardTitle);
            }
            
            // Set the display slot
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            
            // Apply the scoreboard to the player
            player.setScoreboard(scoreboard);

            return ExecutionResult.success("Scoreboard created successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create scoreboard: " + e.getMessage());
        }
    }
    
    /**
     * Gets title from the container configuration
     */
    private String getTitleFromContainer(CodeBlock block, ExecutionContext context) {
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
                        return getTitleFromItem(titleItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting title from container in CreateScoreboardAction: " + e.getMessage());
        }
        
        return "Scoreboard";
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
        return "Scoreboard";
    }
}