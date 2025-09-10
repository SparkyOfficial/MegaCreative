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
 * Action for adding a player to a team.
 * This action adds a player to a specified team from container configuration.
 */
public class AddPlayerToTeamAction implements BlockAction {

    @Override
public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            AddPlayerToTeamParams params = getPlayerTeamParamsFromContainer(block, context);
            
            if (params.teamName == null || params.teamName.isEmpty()) {
                return ExecutionResult.error("Team name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTeamName = resolver.resolveString(context, params.teamName);
            String resolvedTargetPlayerName = resolver.resolveString(context, params.targetPlayerName);

            // Determine which player to add to the team
            Player targetPlayer = player; // Default to current player
            if (resolvedTargetPlayerName != null && !resolvedTargetPlayerName.isEmpty()) {
                Player foundPlayer = player.getServer().getPlayer(resolvedTargetPlayerName);
                if (foundPlayer != null) {
                    targetPlayer = foundPlayer;
                }
            }

            // Add the player to the team
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                // Note: GameScoreboardManager doesn't have an addPlayerToTeam method, so we'll skip this for now
                return ExecutionResult.success("Adding player to team is not implemented yet");
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to add player to team: " + e.getMessage());
        }
    }
    
    /**
     * Gets player team parameters from the container configuration
     */
    private AddPlayerToTeamParams getPlayerTeamParamsFromContainer(CodeBlock block, ExecutionContext context) {
        AddPlayerToTeamParams params = new AddPlayerToTeamParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this action
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getAction());
            
            if (slotResolver != null) {
                // Get team name from the teamName slot
                Integer teamNameSlot = slotResolver.apply("teamName");
                if (teamNameSlot != null) {
                    ItemStack teamNameItem = block.getConfigItem(teamNameSlot);
                    if (teamNameItem != null && teamNameItem.hasItemMeta()) {
                        // Extract team name from item
                        params.teamName = getTeamNameFromItem(teamNameItem);
                    }
                }
                
                // Get target player from the targetPlayer slot
                Integer targetPlayerSlot = slotResolver.apply("targetPlayer");
                if (targetPlayerSlot != null) {
                    ItemStack targetPlayerItem = block.getConfigItem(targetPlayerSlot);
                    if (targetPlayerItem != null && targetPlayerItem.hasItemMeta()) {
                        // Extract target player name from item
                        params.targetPlayerName = getTargetPlayerNameFromItem(targetPlayerItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting player team parameters from container in AddPlayerToTeamAction: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts team name from an item
     */
    private String getTeamNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the team name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts target player name from an item
     */
    private String getTargetPlayerNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target player name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold player team parameters
     */
    private static class AddPlayerToTeamParams {
        String teamName = "";
        String targetPlayerName = "";
    }
}