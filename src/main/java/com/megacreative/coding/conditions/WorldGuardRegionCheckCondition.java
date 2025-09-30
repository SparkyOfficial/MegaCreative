package com.megacreative.coding.conditions;

import com.megacreative.coding.BlockCondition;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.ExecutionContext;
import com.megacreative.coding.ParameterResolver;
import com.megacreative.coding.annotations.BlockMeta;
import com.megacreative.coding.BlockType;
import com.megacreative.coding.values.DataValue;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.World;

@BlockMeta(id = "worldGuardRegionCheck", displayName = "§aWorldGuard Region Check", type = BlockType.CONDITION)
public class WorldGuardRegionCheckCondition implements BlockCondition {

    @Override
    public boolean evaluate(CodeBlock block, ExecutionContext context) {
        Player player = context.getPlayer();
        if (player == null) return false;
        
        try {
            // Get parameters from the new parameter system
            DataValue regionNameValue = block.getParameter("region_name");
            DataValue worldNameValue = block.getParameter("world");
            
            if (regionNameValue == null || regionNameValue.isEmpty()) {
                context.getPlugin().getLogger().warning("WorldGuardRegionCheckCondition: 'region_name' parameter is missing.");
                return false;
            }
            
            // Resolve any placeholders in the parameters
            ParameterResolver resolver = new ParameterResolver(context);
            DataValue resolvedRegionName = resolver.resolve(context, regionNameValue);
            
            String regionName = resolvedRegionName.asString();
            String worldName = null;
            
            if (worldNameValue != null && !worldNameValue.isEmpty()) {
                DataValue resolvedWorldName = resolver.resolve(context, worldNameValue);
                worldName = resolvedWorldName.asString();
            }
            
            worldName = (worldName != null && !worldName.isEmpty()) ? 
                worldName : player.getWorld().getName();
            
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
     * Helper class to hold region parameters
     */
    private static class WorldGuardRegionCheckParams {
        String regionName = "";
        String worldName = "";
    }
}