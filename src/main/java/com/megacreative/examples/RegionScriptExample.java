package com.megacreative.examples;

import com.megacreative.MegaCreative;
import com.megacreative.coding.CodeScript;
import com.megacreative.coding.CodeBlock;
import com.megacreative.coding.events.RegionDetectionSystem;
import com.megacreative.coding.events.CustomEventManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Example demonstrating how to use the Region Script System
 */
public class RegionScriptExample {
    
    /**
     * Sets up a basic region with associated scripts
     */
    public static void setupRegionWithScripts(MegaCreative plugin) {
        // Get the region detection system
        CustomEventManager eventManager = plugin.getServiceRegistry().getCustomEventManager();
        RegionDetectionSystem regionSystem = eventManager.getRegionDetectionSystem();
        
        // Define a spawn region
        World world = plugin.getServer().getWorld("world");
        if (world != null) {
            Location spawnMin = new Location(world, -10, 0, -10);
            Location spawnMax = new Location(world, 10, 256, 10);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "spawn");
            metadata.put("protection", true);
            
            regionSystem.defineRegion("spawn", world.getName(), spawnMin, spawnMax, "Spawn Region", metadata);
            
            // Create an enter script for the spawn region
            CodeBlock enterRoot = new CodeBlock(Material.DIAMOND_BLOCK, "regionEnter");
            // Add actions to the script (this would be more complex in a real implementation)
            
            CodeScript enterScript = new CodeScript("region_spawn_regionEnter", true, enterRoot);
            enterScript.setDescription("Welcome message for players entering spawn");
            regionSystem.setRegionScript("spawn", "regionEnter", enterScript);
            
            // Create an exit script for the spawn region
            CodeBlock exitRoot = new CodeBlock(Material.DIAMOND_BLOCK, "regionExit");
            // Add actions to the script (this would be more complex in a real implementation)
            
            CodeScript exitScript = new CodeScript("region_spawn_regionExit", true, exitRoot);
            exitScript.setDescription("Farewell message for players leaving spawn");
            regionSystem.setRegionScript("spawn", "regionExit", exitScript);
            
            // Create a generic region enter script for all other regions
            CodeBlock genericEnterRoot = new CodeBlock(Material.DIAMOND_BLOCK, "regionEnter");
            // Add generic actions (this would be more complex in a real implementation)
            
            CodeScript genericEnterScript = new CodeScript("region_regionEnter", true, genericEnterRoot);
            genericEnterScript.setDescription("Generic message for entering any region");
            regionSystem.setGenericRegionScript("regionEnter", genericEnterScript);
        }
    }
    
    /**
     * Sets up a circular safezone region
     */
    public static void setupSafezoneRegion(MegaCreative plugin) {
        // Get the region detection system
        CustomEventManager eventManager = plugin.getServiceRegistry().getCustomEventManager();
        RegionDetectionSystem regionSystem = eventManager.getRegionDetectionSystem();
        
        // Define a circular safezone region
        World world = plugin.getServer().getWorld("world");
        if (world != null) {
            Location center = new Location(world, 50, 64, 50);
            double radius = 20.0;
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "safezone");
            metadata.put("pvp", false);
            metadata.put("healing", true);
            
            regionSystem.defineCircularRegion("safezone", world.getName(), center, radius, "Safezone", metadata);
            
            // Create an enter script for the safezone
            CodeBlock enterRoot = new CodeBlock(Material.DIAMOND_BLOCK, "regionEnter");
            // Add actions like: disable PvP, start healing effect, etc.
            
            CodeScript enterScript = new CodeScript("region_safezone_regionEnter", true, enterRoot);
            enterScript.setDescription("Actions when entering safezone");
            regionSystem.setRegionScript("safezone", "regionEnter", enterScript);
            
            // Create an exit script for the safezone
            CodeBlock exitRoot = new CodeBlock(Material.DIAMOND_BLOCK, "regionExit");
            // Add actions like: enable PvP, stop healing effect, etc.
            
            CodeScript exitScript = new CodeScript("region_safezone_regionExit", true, exitRoot);
            exitScript.setDescription("Actions when exiting safezone");
            regionSystem.setRegionScript("safezone", "regionExit", exitScript);
        }
    }
}