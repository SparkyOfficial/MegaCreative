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
        
        CustomEventManager eventManager = plugin.getServiceRegistry().getCustomEventManager();
        RegionDetectionSystem regionSystem = eventManager.getRegionDetectionSystem();
        
        
        World world = plugin.getServer().getWorld("world");
        if (world != null) {
            Location spawnMin = new Location(world, -10, 0, -10);
            Location spawnMax = new Location(world, 10, 256, 10);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "spawn");
            metadata.put("protection", true);
            
            regionSystem.defineRegion("spawn", world.getName(), spawnMin, spawnMax, "Spawn Region", metadata);
            
            
            CodeBlock enterRoot = new CodeBlock("DIAMOND_BLOCK", "regionEnter");
            
            
            CodeScript enterScript = new CodeScript("region_spawn_regionEnter", true, enterRoot);
            enterScript.setDescription("Welcome message for players entering spawn");
            regionSystem.setRegionScript("spawn", "regionEnter", enterScript);
            
            
            CodeBlock exitRoot = new CodeBlock("DIAMOND_BLOCK", "regionExit");
            
            
            CodeScript exitScript = new CodeScript("region_spawn_regionExit", true, exitRoot);
            exitScript.setDescription("Farewell message for players leaving spawn");
            regionSystem.setRegionScript("spawn", "regionExit", exitScript);
            
            
            CodeBlock genericEnterRoot = new CodeBlock("DIAMOND_BLOCK", "regionEnter");
            
            
            CodeScript genericEnterScript = new CodeScript("region_regionEnter", true, genericEnterRoot);
            genericEnterScript.setDescription("Generic message for entering any region");
            regionSystem.setGenericRegionScript("regionEnter", genericEnterScript);
        }
    }
    
    /**
     * Sets up a circular safezone region
     */
    public static void setupSafezoneRegion(MegaCreative plugin) {
        
        CustomEventManager eventManager = plugin.getServiceRegistry().getCustomEventManager();
        RegionDetectionSystem regionSystem = eventManager.getRegionDetectionSystem();
        
        
        World world = plugin.getServer().getWorld("world");
        if (world != null) {
            Location center = new Location(world, 50, 64, 50);
            double radius = 20.0;
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "safezone");
            metadata.put("pvp", false);
            metadata.put("healing", true);
            
            regionSystem.defineCircularRegion("safezone", world.getName(), center, radius, "Safezone", metadata);
            
            
            CodeBlock enterRoot = new CodeBlock("DIAMOND_BLOCK", "regionEnter");
            
            
            CodeScript enterScript = new CodeScript("region_safezone_regionEnter", true, enterRoot);
            enterScript.setDescription("Actions when entering safezone");
            regionSystem.setRegionScript("safezone", "regionEnter", enterScript);
            
            
            CodeBlock exitRoot = new CodeBlock("DIAMOND_BLOCK", "regionExit");
            
            
            CodeScript exitScript = new CodeScript("region_safezone_regionExit", true, exitRoot);
            exitScript.setDescription("Actions when exiting safezone");
            regionSystem.setRegionScript("safezone", "regionExit", exitScript);
        }
    }
}