package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

/**
 * Condition for checking if a player is in a specific game mode from container configuration.
 * This condition returns true if the player is in the specified game mode.
 */
@BlockMeta(id = "playerGameMode", displayName = "§aPlayer Game Mode", type = BlockType.CONDITION)
public class PlayerGameModeCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }

        try {
            // Get parameters from the container configuration
            PlayerGameModeParams params = getGameModeParamsFromContainer(block, context);
            
            // Parse game mode parameter
            String modeName = params.modeStr;
            if (modeName == null || modeName.isEmpty()) {
                return false;
            }

            // Check if player is in the specified game mode
            try {
                GameMode gameMode = GameMode.valueOf(modeName.toUpperCase());
                return player.getGameMode() == gameMode;
            } catch (IllegalArgumentException e) {
                return false;
            }
        } catch (Exception e) {
            // If there's an error, return false
            return false;
        }
    }
    
    /**
     * Gets game mode parameters from the container configuration
     */
    private PlayerGameModeParams getGameModeParamsFromContainer(CodeBlock block, ExecutionContext context) {
        PlayerGameModeParams params = new PlayerGameModeParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get game mode from the mode_slot
                Integer modeSlot = slotResolver.apply("mode_slot");
                if (modeSlot != null) {
                    ItemStack modeItem = block.getConfigItem(modeSlot);
                    if (modeItem != null && modeItem.hasItemMeta()) {
                        // Extract game mode from item
                        params.modeStr = getGameModeFromItem(modeItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting game mode parameters from container in PlayerGameModeCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts game mode from an item
     */
    private String getGameModeFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the game mode
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold game mode parameters
     */
    private static class PlayerGameModeParams {
        String modeStr = "";
    }
}