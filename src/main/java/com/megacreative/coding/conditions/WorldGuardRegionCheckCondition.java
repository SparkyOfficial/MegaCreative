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

import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;

@BlockMeta(id = "worldGuardRegionCheck", displayName = "§aWorldGuard Region Check", type = BlockType.CONDITION)
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
            
            // Try to get WorldGuard API
            try {
                // Use reflection to access WorldGuard API to avoid direct dependency
                Class<?> worldGuardClass = Class.forName("com.sk89q.worldguard.WorldGuard");
                Object worldGuardInstance = worldGuardClass.getMethod("getInstance").invoke(null);
                Object platform = worldGuardInstance.getClass().getMethod("getPlatform").invoke(worldGuardInstance);
                Object regionContainer = platform.getClass().getMethod("getRegionContainer").invoke(platform);
                
                // Get the world wrapper
                Class<?> worldEditWorldClass = Class.forName("com.sk89q.worldedit.world.World");
                Class<?> bukkitAdapterClass = Class.forName("com.sk89q.worldedit.bukkit.BukkitAdapter");
                Object worldEditWorld = bukkitAdapterClass.getMethod("adapt", World.class).invoke(null, world);
                
                // Get region manager for the world
                Object regionManager = regionContainer.getClass().getMethod("get", worldEditWorldClass).invoke(regionContainer, worldEditWorld);
                if (regionManager == null) {
                    context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: No region manager for world '" + worldName + "'.");
                    return false;
                }
                
                // Get the region
                Class<?> protectedRegionClass = Class.forName("com.sk89q.worldguard.protection.regions.ProtectedRegion");
                Object region = regionManager.getClass().getMethod("getRegion", String.class).invoke(regionManager, regionName);
                if (region == null) {
                    context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: Region '" + regionName + "' not found in world '" + worldName + "'.");
                    return false;
                }
                
                // Check if player is in the region
                Class<?> locationClass = Class.forName("com.sk89q.worldedit.util.Location");
                Class<?> vector3Class = Class.forName("com.sk89q.worldedit.math.Vector3");
                Object playerLocation = bukkitAdapterClass.getMethod("adapt", org.bukkit.Location.class).invoke(null, player.getLocation());
                Object vector3 = vector3Class.getMethod("at", double.class, double.class, double.class)
                    .invoke(null, player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ());
                Object weLocation = locationClass.getConstructor(worldEditWorldClass, vector3Class).newInstance(worldEditWorld, vector3);
                
                boolean isInsideRegion = (Boolean) region.getClass().getMethod("contains", vector3Class).invoke(region, vector3);
                return isInsideRegion;
                
            } catch (Exception e) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: Failed to check region '" + regionName + "' - " + e.getMessage());
                return false;
            }

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