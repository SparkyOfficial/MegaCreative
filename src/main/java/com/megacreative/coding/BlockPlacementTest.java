package com.megacreative.coding;

import com.megacreative.MegaCreative;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Test class to verify block placement functionality
 */
public class BlockPlacementTest {
    
    /**
     * Creates a simple test script by placing blocks in the world
     */
    public static void createTestScript(MegaCreative plugin, Player player) {
        try {
            World world = player.getWorld();
            
            // Get the player's current location as the starting point
            Location startLoc = player.getLocation().clone();
            
            // Make sure we're placing blocks on a valid surface (glass platform)
            startLoc.setY(64); // Platform height
            
            // Round coordinates to integers
            startLoc.setX(Math.round(startLoc.getX()));
            startLoc.setZ(Math.round(startLoc.getZ()));
            
            plugin.getLogger().info("Creating test script at coordinates: " + 
                startLoc.getBlockX() + ", " + startLoc.getBlockY() + ", " + startLoc.getBlockZ());
            
            // Get the block placement handler
            BlockPlacementHandler placementHandler = plugin.getServiceRegistry().getBlockPlacementHandler();
            
            // Place event block (diamond block for player join)
            Location eventBlockLoc = startLoc.clone();
            Block eventBlock = eventBlockLoc.getBlock();
            eventBlock.setType(Material.DIAMOND_BLOCK);
            
            // Simulate block placement event
            BlockPlaceEvent eventBlockEvent = new BlockPlaceEvent(
                eventBlock,
                eventBlock.getState(),
                eventBlockLoc.clone().add(0, -1, 0).getBlock(), // Placed against
                new ItemStack(Material.DIAMOND_BLOCK),
                player,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND
            );
            
            // Call the event to trigger block placement handler
            Bukkit.getPluginManager().callEvent(eventBlockEvent);
            
            // Place action block (cobblestone for send message) to the right
            Location actionBlockLoc = startLoc.clone().add(1, 0, 0);
            Block actionBlock = actionBlockLoc.getBlock();
            actionBlock.setType(Material.COBBLESTONE);
            
            // Simulate block placement event
            BlockPlaceEvent actionBlockEvent = new BlockPlaceEvent(
                actionBlock,
                actionBlock.getState(),
                actionBlockLoc.clone().add(0, -1, 0).getBlock(), // Placed against
                new ItemStack(Material.COBBLESTONE),
                player,
                true,
                org.bukkit.inventory.EquipmentSlot.HAND
            );
            
            // Call the event to trigger block placement handler
            Bukkit.getPluginManager().callEvent(actionBlockEvent);
            
            plugin.getLogger().info("Test script created successfully!");
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating test script: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Tests the compilation of the test script
     */
    public static void testCompilation(MegaCreative plugin, World world) {
        try {
            // Create the script compiler
            ScriptCompiler compiler = new ScriptCompiler(
                plugin,
                plugin.getServiceRegistry().getBlockConfigService(),
                plugin.getServiceRegistry().getBlockPlacementHandler()
            );
            
            // Compile all scripts in the world
            java.util.List<CodeScript> scripts = compiler.compileWorldScripts(world);
            
            plugin.getLogger().info("Successfully compiled " + scripts.size() + " scripts");
            
            // Print script structure for debugging
            compiler.printScriptStructure(scripts);
            
        } catch (Exception e) {
            plugin.getLogger().severe("Error testing script compilation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}