package com.megacreative.coding.actions;

import com.megacreative.coding.BlockAction;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.executors.ExecutionResult;
import com.megacreative.services.BlockConfigService;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

/**
 * Action for setting a player's game mode.
 * This action changes the player's game mode based on the container configuration.
 */
public class SetGameModeAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get the game mode from the container configuration
            GameMode gameMode = getGameModeFromContainer(block, context);
            
            if (gameMode == null) {
                return ExecutionResult.error("Game mode is not configured");
            }

            // Set the game mode
            player.setGameMode(gameMode);
            return ExecutionResult.success("Game mode set to " + gameMode.name());
        } catch (Exception e) {
            return ExecutionResult.error("Failed to set game mode: " + e.getMessage());
        }
    }
    
    /**
     * Gets game mode from the container configuration
     */
    private GameMode getGameModeFromContainer(CodeBlock block, ExecutionContext context) {
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get mode from the mode slot
                Integer modeSlot = slotResolver.apply("mode_slot");
                if (modeSlot != null) {
                    ItemStack modeItem = block.getConfigItem(modeSlot);
                    if (modeItem != null && modeItem.hasItemMeta()) {
                        // Extract mode from item
                        String modeName = getModeNameFromItem(modeItem);
                        if (modeName != null) {
                            return GameMode.valueOf(modeName.toUpperCase());
                        }
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting game mode from container in SetGameModeAction: " + e.getMessage());
        }
        
        return null;
    }
    
    /**
     * Extracts mode name from an item
     */
    private String getModeNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the mode name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
}