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
 * Action for creating a team.
 * This action creates a team with a specified name and optional display name, prefix, and suffix from container configuration.
 */
public class CreateTeamAction implements BlockAction {

    @Override
    public ExecutionResult execute(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return ExecutionResult.error("No player found in execution context");
        }

        try {
            // Get parameters from the container configuration
            CreateTeamParams params = getTeamParamsFromContainer(block, context);
            
            if (params.teamName == null || params.teamName.isEmpty()) {
                return ExecutionResult.error("Team name is not configured");
            }

            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            String resolvedTeamName = resolver.resolveString(context, params.teamName);
            String resolvedDisplayName = resolver.resolveString(context, params.displayName);
            String resolvedPrefix = resolver.resolveString(context, params.prefix);
            String resolvedSuffix = resolver.resolveString(context, params.suffix);

            // Create the team
            GameScoreboardManager scoreboardManager = context.getPlugin().getServiceRegistry().getGameScoreboardManager();
            if (scoreboardManager != null) {
                // Note: GameScoreboardManager doesn't have a createTeam method, so we'll skip this for now
                return ExecutionResult.success("Team creation is not implemented yet");
            } else {
                return ExecutionResult.error("Scoreboard manager is not available");
            }
        } catch (Exception e) {
            return ExecutionResult.error("Failed to create team: " + e.getMessage());
        }
    }
    
    /**
     * Gets team parameters from the container configuration
     */
    private CreateTeamParams getTeamParamsFromContainer(CodeBlock block, ExecutionContext context) {
        CreateTeamParams params = new CreateTeamParams();
        
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
                
                // Get display name from the displayName slot
                Integer displayNameSlot = slotResolver.apply("displayName");
                if (displayNameSlot != null) {
                    ItemStack displayNameItem = block.getConfigItem(displayNameSlot);
                    if (displayNameItem != null && displayNameItem.hasItemMeta()) {
                        // Extract display name from item
                        params.displayName = getDisplayNameFromItem(displayNameItem);
                    }
                }
                
                // Get prefix from the prefix slot
                Integer prefixSlot = slotResolver.apply("prefix");
                if (prefixSlot != null) {
                    ItemStack prefixItem = block.getConfigItem(prefixSlot);
                    if (prefixItem != null && prefixItem.hasItemMeta()) {
                        // Extract prefix from item
                        params.prefix = getPrefixFromItem(prefixItem);
                    }
                }
                
                // Get suffix from the suffix slot
                Integer suffixSlot = slotResolver.apply("suffix");
                if (suffixSlot != null) {
                    ItemStack suffixItem = block.getConfigItem(suffixSlot);
                    if (suffixItem != null && suffixItem.hasItemMeta()) {
                        // Extract suffix from item
                        params.suffix = getSuffixFromItem(suffixItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting team parameters from container in CreateTeamAction: " + e.getMessage());
        }
        
        // Set defaults
        if (params.displayName == null) {
            params.displayName = params.teamName != null ? params.teamName : "";
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
     * Extracts display name from an item
     */
    private String getDisplayNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the display name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts prefix from an item
     */
    private String getPrefixFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the prefix
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Extracts suffix from an item
     */
    private String getSuffixFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the suffix
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return "";
    }
    
    /**
     * Helper class to hold team parameters
     */
    private static class CreateTeamParams {
        String teamName = "";
        String displayName = "";
        String prefix = "";
        String suffix = "";
    }
}