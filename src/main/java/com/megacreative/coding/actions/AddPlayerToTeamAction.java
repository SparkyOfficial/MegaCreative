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
import org.bukkit.scoreboard.Team;

import java.util.function.Function;

/**
 * Action for adding a player to a team.
 * This action retrieves parameters from the container configuration and adds a player to a team.
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
            AddPlayerToTeamParams params = getPlayerToTeamParamsFromContainer(block, context);

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue teamNameVal = DataValue.of(params.teamNameStr);
            DataValue resolvedTeamName = resolver.resolve(context, teamNameVal);
            
            DataValue targetPlayerVal = DataValue.of(params.targetPlayerStr);
            DataValue resolvedTargetPlayer = resolver.resolve(context, targetPlayerVal);
            
            // Parse parameters
            String teamName = resolvedTeamName.asString();
            String targetPlayer = resolvedTargetPlayer.asString();
            
            if (teamName == null || teamName.isEmpty()) {
                return ExecutionResult.error("Invalid team name");
            }

            // If no target player is specified, use the current player
            if (targetPlayer == null || targetPlayer.isEmpty()) {
                targetPlayer = player.getName();
            }

            // Add player to the team using the scoreboard system
            Scoreboard scoreboard = player.getScoreboard();
            Team team = scoreboard.getTeam(teamName);
            
            if (team == null) {
                return ExecutionResult.error("Team not found: " + teamName);
            }
            
            // Add the player to the team
            team.addEntry(targetPlayer);

            return ExecutionResult.success("Player added to team successfully");
        } catch (Exception e) {
            return ExecutionResult.error("Failed to add player to team: " + e.getMessage());
        }
    }
    
    /**
     * Gets player to team parameters from the container configuration
     */
    private AddPlayerToTeamParams getPlayerToTeamParamsFromContainer(CodeBlock block, ExecutionContext context) {
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
                        params.teamNameStr = getTeamNameFromItem(teamNameItem);
                    }
                }
                
                // Get target player from the targetPlayer slot
                Integer targetPlayerSlot = slotResolver.apply("targetPlayer");
                if (targetPlayerSlot != null) {
                    ItemStack targetPlayerItem = block.getConfigItem(targetPlayerSlot);
                    if (targetPlayerItem != null && targetPlayerItem.hasItemMeta()) {
                        // Extract target player from item
                        params.targetPlayerStr = getTargetPlayerFromItem(targetPlayerItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting player to team parameters from container in AddPlayerToTeamAction: " + e.getMessage());
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
        return "";
    }
    
    /**
     * Extracts target player from an item
     */
    private String getTargetPlayerFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the target player
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Helper class to hold player to team parameters
     */
    private static class AddPlayerToTeamParams {
        String teamNameStr = "";
        String targetPlayerStr = "";
    }
}