package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.managers.GameScoreboardManager;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for creating a scoreboard.
 * This action creates a scoreboard with a specified title from container configuration.
 */
public class CreateScoreboardAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the title from the container configuration
            String title = getTitleFromContainer(block, context);
            if (title == null || title.isEmpty()) {
                return ExecutionResult.error("Title is not configured");
            }

            // Resolve any placeholders in the title
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTitle = resolver.resolveString(context, title);

            // Create the scoreboard
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                scoreboardManager.createPlayerScoreboard(player, resolvedTitle);
                return ExecutionResult.success("Created scoreboard with title: " + resolvedTitle);
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
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
        
        return null;
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
        return null;
    }
}