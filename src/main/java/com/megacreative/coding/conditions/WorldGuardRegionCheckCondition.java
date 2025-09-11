package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.services.BlockConfigService;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.function.Function;

public class WorldGuardRegionCheckCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            // Get parameters from the container configuration
            WorldGuardRegionCheckParams params = getRegionParamsFromContainer(block, context);
            
            if (params.regionName == null || params.regionName.isEmpty()) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: 'region_name' parameter is missing.");
                return false;
            }
            
            String regionName = params.regionName;
            String worldName = (params.worldName != null && !params.worldName.isEmpty()) ? 
                params.worldName : player.getWorld().getName();
            
            // Check if WorldGuard is available
            if (!Bukkit.getPluginManager().isPluginEnabled("WorldGuard")) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: WorldGuard is not installed or enabled.");
                return false;
            }
            
            // Get the world
            World world = Bukkit.getWorld(worldName);
            if (world == null) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: World '" + worldName + "' not found.");
                return false;
            }
            
            // For now, we'll return false as we don't have direct access to WorldGuard API
            // In a real implementation, you would check if the player is in the specified region
            context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: WorldGuard integration not fully implemented.");
            return false;

        } catch (Exception e) {
            context.getPlugin().getLogger().severe("Error evaluating WorldGuardRegionCheckCondition: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Gets region parameters from the container configuration
     */
    private WorldGuardRegionCheckParams getRegionParamsFromContainer(CodeBlock block, ExecutionContext context) {
        WorldGuardRegionCheckParams params = new WorldGuardRegionCheckParams();
        
        try {
            // Get the BlockConfigService to resolve slot names
            BlockConfigService blockConfigService = context.getPlugin().getServiceRegistry().getBlockConfigService();
            
            // Get the slot resolver for this condition
            Function<String, Integer> slotResolver = blockConfigService.getSlotResolver(block.getCondition());
            
            if (slotResolver != null) {
                // Get region name from the region_name_slot
                Integer regionNameSlot = slotResolver.apply("region_name_slot");
                if (regionNameSlot != null) {
                    ItemStack regionNameItem = block.getConfigItem(regionNameSlot);
                    if (regionNameItem != null && regionNameItem.hasItemMeta()) {
                        // Extract region name from item
                        params.regionName = getRegionNameFromItem(regionNameItem);
                    }
                }
                
                // Get world from the world_slot
                Integer worldSlot = slotResolver.apply("world_slot");
                if (worldSlot != null) {
                    ItemStack worldItem = block.getConfigItem(worldSlot);
                    if (worldItem != null && worldItem.hasItemMeta()) {
                        // Extract world name from item
                        params.worldName = getWorldNameFromItem(worldItem);
                    }
                }
            }
        } catch (Exception e) {
            context.getPlugin().getLogger().warning("Error getting region parameters from container in WorldGuardRegionCheckCondition: " + e.getMessage());
        }
        
        return params;
    }
    
    /**
     * Extracts region name from an item
     */
    private String getRegionNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the region name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Extracts world name from an item
     */
    private String getWorldNameFromItem(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            String displayName = meta.getDisplayName();
            if (displayName != null && !displayName.isEmpty()) {
                // Remove color codes and return the world name
                return displayName.replaceAll("[§0-9]", "").trim();
            }
        }
        return null;
    }
    
    /**
     * Helper class to hold region parameters
     */
    private static class WorldGuardRegionCheckParams {
        String regionName = "";
        String worldName = "";
    }
}